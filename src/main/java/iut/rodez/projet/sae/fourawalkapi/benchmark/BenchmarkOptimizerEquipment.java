import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Benchmark autonome — OptimizerService (équipements)
 * Algorithme : Couverture d'ensemble par Backtracking binaire (inclusion / exclusion)
 *              Résolution par catégorie — Map<TypeEquipment, GroupEquipment>
 *
 * Plateforme de mesure :
 *   CPU  : Intel Core i9 Gen 14
 *   RAM  : 32 Go DDR5
 *   JVM  : OpenJDK 21+ (HotSpot)
 *
 * Exécution :
 *   javac BenchmarkOptimizerEquipment.java && java -Xms256m -Xmx2g BenchmarkOptimizerEquipment
 */
public class BenchmarkOptimizerEquipment {

    enum TypeEquipment { SOIN, PROGRESSION, EAU, REPOS, VETEMENT, AUTRE }

    static class EquipmentItem {
        final int nbItem;
        final double masseGrammes;
        final String name;

        EquipmentItem(String name, int nbItem, double masse) {
            this.name = name;
            this.nbItem = nbItem;
            this.masseGrammes = masse;
        }
    }

    // ── Algorithme : arbre binaire inclusion/exclusion ────────────────────────

    static List<EquipmentItem> getOptimizeAllEquipment(
            Map<TypeEquipment, List<EquipmentItem>> equipmentGroups,
            int nbParticipants,
            boolean multiDay) {

        List<TypeEquipment> typeList = new ArrayList<>(Arrays.asList(TypeEquipment.values()));
        if (!multiDay) typeList.remove(TypeEquipment.REPOS);

        List<EquipmentItem> result = new ArrayList<>();

        for (TypeEquipment type : typeList) {
            List<EquipmentItem> items = equipmentGroups.get(type);
            if (items == null) continue;

            if (type == TypeEquipment.VETEMENT || type == TypeEquipment.AUTRE) {
                result.addAll(items);     // pas d'optimisation, ajout direct
                continue;
            }

            List<EquipmentItem> best = sortBest(items, new ArrayList<>(), nbParticipants, 0);
            if (best == null)
                throw new RuntimeException("Couverture impossible pour : " + type);
            result.addAll(best);
        }
        return result;
    }

    static List<EquipmentItem> sortBest(List<EquipmentItem> candidates,
                                         List<EquipmentItem> current,
                                         int target, int index) {
        int coverage = current.stream().mapToInt(i -> i.nbItem).sum();
        if (coverage >= target) return new ArrayList<>(current);
        if (index >= candidates.size()) return null;

        EquipmentItem item = candidates.get(index);

        // Branche inclusion
        current.add(item);
        List<EquipmentItem> take = sortBest(candidates, current, target, index + 1);
        current.remove(current.size() - 1);

        // Branche exclusion
        List<EquipmentItem> skip = sortBest(candidates, current, target, index + 1);

        if (take == null) return skip;
        if (skip == null) return take;

        // Arbitrage : favoriser la sélection la plus diverse (plus d'items distincts)
        return take.size() >= skip.size() ? take : skip;
    }

    // ── Génération des données ────────────────────────────────────────────────

    static Map<TypeEquipment, List<EquipmentItem>> generateCatalogue(
            int itemsPerCategory, int nbParticipants, Random rng) {

        Map<TypeEquipment, List<EquipmentItem>> map = new EnumMap<>(TypeEquipment.class);
        String[] names = {"alpha","beta","gamma","delta","epsilon","zeta","eta","theta",
                          "iota","kappa","lambda","mu","nu","xi","omicron","pi"};

        for (TypeEquipment type : TypeEquipment.values()) {
            List<EquipmentItem> items = new ArrayList<>();
            int remaining = nbParticipants;
            // Garantir qu'une solution existe : dernier item couvre tout le reste
            for (int i = 0; i < itemsPerCategory - 1 && remaining > 0; i++) {
                int covers = 1 + rng.nextInt(Math.min(remaining, 3));
                items.add(new EquipmentItem(
                    names[i % names.length] + "_" + type,
                    covers,
                    200 + rng.nextDouble() * 800));
                remaining -= covers;
            }
            if (remaining > 0)
                items.add(new EquipmentItem("last_" + type, remaining, 150 + rng.nextDouble() * 400));
            map.put(type, items);
        }
        return map;
    }

    // ── Résultat par run ──────────────────────────────────────────────────────

    static class BenchResult {
        final int c, p, run;
        final long timeNs, heapBytes;
        final boolean success;
        BenchResult(int c, int p, int run, long t, long h, boolean s) {
            this.c=c; this.p=p; this.run=run; this.timeNs=t; this.heapBytes=h; this.success=s;
        }
    }

    // ── Point d'entrée ────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║   BENCHMARK — OptimizerService (Équipements)                     ║");
        System.out.println("║   Couverture d'ensemble — arbre binaire inclusion/exclusion       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("  CPU : Intel Core i9 Gen 14   |   RAM : 32 Go DDR5");
        System.out.printf ("  JVM : %s %s%n%n",
            System.getProperty("java.vm.name"), System.getProperty("java.version"));

        // (c items/catégorie, p participants)
        int[][] scenarios = {
            { 3,  4}, { 5,  4}, { 6,  6},
            { 8,  6}, {10,  8}, {12,  8},
            {15, 10}, {18, 10}, {20, 12},
        };

        final int WARMUP = 3, RUNS = 10;
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        List<BenchResult> allResults = new ArrayList<>();

        System.out.printf("%-18s %-8s %-14s %-12s %-14s %-12s%n",
            "Scénario", "Runs", "Tps médian", "Heap moy.", "Nœuds théo.", "Tps max");
        System.out.println("─".repeat(82));

        for (int[] sc : scenarios) {
            int c = sc[0], p = sc[1];

            for (int w = 0; w < WARMUP; w++) {
                try {
                    getOptimizeAllEquipment(
                        generateCatalogue(c, p, new Random(w)), p, true);
                } catch (Exception ignored) {}
            }

            long[] times = new long[RUNS];
            long[] heaps = new long[RUNS];
            boolean allOk = true;

            for (int r = 0; r < RUNS; r++) {
                Map<TypeEquipment, List<EquipmentItem>> catalogue =
                    generateCatalogue(c, p, new Random(r * 100 + c));
                System.gc(); pause(10);

                MemoryUsage before = mem.getHeapMemoryUsage();
                long t0 = System.nanoTime();
                boolean ok = true;
                try { getOptimizeAllEquipment(catalogue, p, true); }
                catch (Exception e) { ok = false; allOk = false; }
                long t1 = System.nanoTime();
                MemoryUsage after = mem.getHeapMemoryUsage();

                times[r] = t1 - t0;
                heaps[r] = Math.max(0, after.getUsed() - before.getUsed());
                allResults.add(new BenchResult(c, p, r, times[r], heaps[r], ok));
            }

            Arrays.sort(times);
            long median  = times[RUNS / 2];
            long avgHeap = Arrays.stream(heaps).sum() / RUNS;
            // Nœuds théoriques = m catégories × 2^c (m = 4 catégories non-triviales)
            long theoreticalNodes = 4L * (1L << c);

            System.out.printf("c=%-3d p=%-3d       %-8d  %-14s %-12s %-14s %-12s%s%n",
                c, p, RUNS,
                fmtNs(median), fmtBytes(avgHeap),
                theoreticalNodes > 1_000_000 ? ">1M" : String.valueOf(theoreticalNodes),
                fmtNs(times[RUNS-1]),
                allOk ? "" : " ⚠ échec");
        }

        System.out.println();
        System.out.println("═".repeat(82));
        System.out.println("  MODÈLE THÉORIQUE : T = O(m · 2^c)   S = O(c)");
        System.out.println("  m = nb catégories non triviales (max 4 : SOIN, PROGRESSION, EAU, REPOS)");
        System.out.println("  c = nb items par catégorie (≤ 10 typique en production)");
        System.out.println("  Aucun surcoût de copies : currentSelection partagée par référence.");
        System.out.println("═".repeat(82));

        // Arbre de décision illustratif
        System.out.println();
        System.out.println("  STRUCTURE DE L'ARBRE POUR c=3, 1 catégorie :");
        System.out.println("                       []");
        System.out.println("                 /          \\");
        System.out.println("           [item1]            []");
        System.out.println("           /    \\            /  \\");
        System.out.println("      [i1,i2] [i1]       [i2]   []");
        System.out.println("      /  \\    / \\         ...   ...");
        System.out.println("  [i1,i2,i3] ... ...   8 feuilles max par catégorie");

        exportCsv(allResults, "benchmark_optimizer_equipment_results.csv");
        System.out.println("\n  Résultats bruts exportés → benchmark_optimizer_equipment_results.csv");
    }

    static String fmtNs(long ns) {
        if (ns < 1_000L)         return ns + " ns";
        if (ns < 1_000_000L)     return String.format("%.2f µs", ns / 1_000.0);
        if (ns < 1_000_000_000L) return String.format("%.2f ms", ns / 1_000_000.0);
        return                          String.format("%.3f s",   ns / 1_000_000_000.0);
    }

    static String fmtBytes(long b) {
        if (b <= 0)          return "~0";
        if (b < 1_024L)      return b + " o";
        if (b < 1_048_576L)  return String.format("%.1f Ko", b / 1_024.0);
        return                      String.format("%.2f Mo",  b / 1_048_576.0);
    }

    static void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static void exportCsv(List<BenchResult> results, String filename) {
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("c,p,run,temps_ns,temps_ms,heap_bytes,heap_ko,success\n");
            for (BenchResult r : results)
                fw.write(String.format("%d,%d,%d,%d,%.4f,%d,%.2f,%s%n",
                    r.c, r.p, r.run, r.timeNs, r.timeNs / 1_000_000.0,
                    r.heapBytes, r.heapBytes / 1_024.0, r.success));
        } catch (IOException e) {
            System.err.println("Export CSV échoué : " + e.getMessage());
        }
    }
}
