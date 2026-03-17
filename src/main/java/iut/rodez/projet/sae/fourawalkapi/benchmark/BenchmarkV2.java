import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Benchmark autonome — BackpackDistributorServiceV2
 * Algorithme : Bin Packing par Backtracking + tri glouton des candidats à chaque nœud
 *
 * Plateforme de mesure :
 *   CPU  : Intel Core i9 Gen 14
 *   RAM  : 32 Go DDR5
 *   JVM  : OpenJDK 21+ (HotSpot)
 *
 * Exécution :
 *   javac BenchmarkV2.java && java -Xms512m -Xmx4g BenchmarkV2
 */
public class BenchmarkV2 {

    static class Item {
        final double masseGrammes;
        final int nbItem;
        final String type;
        final Long ownerId;

        Item(double masse, int nb, String type, Long ownerId) {
            this.masseGrammes = masse;
            this.nbItem       = nb;
            this.type         = type;
            this.ownerId      = ownerId;
        }

        double totalWeight() { return masseGrammes * nbItem; }
    }

    static class Backpack {
        final double capaciteMaxGrammes;
        final List<Item> content = new ArrayList<>();
        double currentWeight = 0;
        final Long ownerId;

        Backpack(double capacite, Long ownerId) {
            this.capaciteMaxGrammes = capacite;
            this.ownerId            = ownerId;
        }

        void clearContent()      { content.clear(); currentWeight = 0; }
        boolean canAdd(double w) { return currentWeight + w <= capaciteMaxGrammes; }
        void addItem(Item i)     { content.add(i); currentWeight += i.totalWeight(); }
        void removeItem(Item i)  { content.remove(i); currentWeight -= i.totalWeight(); }
        double spaceRemaining()  { return capaciteMaxGrammes - currentWeight; }
    }

    static void distribute(List<Item> items, List<Backpack> backpacks) {
        backpacks.forEach(Backpack::clearContent);
        items.sort((a, b) -> Double.compare(b.totalWeight(), a.totalWeight()));
        if (!solve(0, items, backpacks))
            throw new RuntimeException("Répartition impossible");
    }

    static boolean solve(int index, List<Item> items, List<Backpack> backpacks) {
        if (index >= items.size()) return true;

        Item cur = items.get(index);
        double w = cur.totalWeight();

        Backpack preferred = null;
        if ("VETEMENT".equals(cur.type) || "REPOS".equals(cur.type))
            for (Backpack b : backpacks)
                if (Objects.equals(b.ownerId, cur.ownerId)) { preferred = b; break; }

        // Tri décroissant par espace disponible — coût O(k log k) par nœud
        List<Backpack> candidates = new ArrayList<>(backpacks);
        candidates.sort((a, b) -> Double.compare(b.spaceRemaining(), a.spaceRemaining()));

        if (preferred != null) {
            candidates.remove(preferred);
            candidates.add(0, preferred);
        }

        for (Backpack bp : candidates) {
            if (bp.canAdd(w)) {
                bp.addItem(cur);
                if (solve(index + 1, items, backpacks)) return true;
                bp.removeItem(cur);
            }
        }
        return false;
    }

    static List<Item> generateItems(int n, Random rng) {
        String[] types = {"SOIN", "PROGRESSION", "EAU", "VETEMENT", "REPOS"};
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < n; i++)
            list.add(new Item(
                50 + rng.nextDouble() * 950,
                1 + rng.nextInt(3),
                types[rng.nextInt(types.length)],
                (long)(rng.nextInt(4) + 1)));
        return list;
    }

    static List<Backpack> generateBackpacks(int k, int n, Random rng) {
        double totalW = generateItems(n, rng).stream().mapToDouble(Item::totalWeight).sum();
        double perBag = (totalW / k) * 1.3;
        List<Backpack> bps = new ArrayList<>();
        for (int i = 0; i < k; i++)
            bps.add(new Backpack(perBag + rng.nextDouble() * 500, (long)(i + 1)));
        return bps;
    }

    static class BenchResult {
        final int n, k, run;
        final long timeNs, heapBytes;
        final boolean success;
        BenchResult(int n, int k, int run, long t, long h, boolean s) {
            this.n=n; this.k=k; this.run=run; this.timeNs=t; this.heapBytes=h; this.success=s;
        }
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║   BENCHMARK — BackpackDistributorServiceV2                       ║");
        System.out.println("║   Backtracking + tri glouton O(k log k) par nœud                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("  CPU : Intel Core i9 Gen 14   |   RAM : 32 Go DDR5");
        System.out.printf ("  JVM : %s %s%n%n",
            System.getProperty("java.vm.name"), System.getProperty("java.version"));

        int[][] scenarios = {
            { 5, 3}, { 8, 3}, {10, 4}, {12, 4},
            {15, 4}, {18, 5}, {20, 5}, {22, 5},
            {25, 5}, {28, 5},
        };

        final int WARMUP = 3, RUNS = 8;
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        List<BenchResult> allResults = new ArrayList<>();

        System.out.printf("%-14s %-8s %-14s %-14s %-14s %-12s%n",
            "Scénario", "Runs", "Tps médian", "Tps min", "Tps max", "Heap moy.");
        System.out.println("─".repeat(80));

        for (int[] sc : scenarios) {
            int n = sc[0], k = sc[1];
            for (int w = 0; w < WARMUP; w++) {
                try { distribute(generateItems(n, new Random(w)), generateBackpacks(k, n, new Random(w))); }
                catch (Exception ignored) {}
            }

            long[] times = new long[RUNS];
            long[] heaps = new long[RUNS];
            boolean allOk = true;

            for (int r = 0; r < RUNS; r++) {
                List<Item>     items = generateItems(n, new Random(r * 100 + n));
                List<Backpack> bps   = generateBackpacks(k, n, new Random(r * 100 + n));
                System.gc(); pause(15);

                MemoryUsage before = mem.getHeapMemoryUsage();
                long t0 = System.nanoTime();
                boolean ok = true;
                try { distribute(items, bps); } catch (Exception e) { ok = false; allOk = false; }
                long t1 = System.nanoTime();
                MemoryUsage after = mem.getHeapMemoryUsage();

                times[r] = t1 - t0;
                heaps[r] = Math.max(0, after.getUsed() - before.getUsed());
                allResults.add(new BenchResult(n, k, r, times[r], heaps[r], ok));
            }

            Arrays.sort(times);
            long median  = times[RUNS / 2];
            long avgHeap = Arrays.stream(heaps).sum() / RUNS;

            System.out.printf("n=%-3d k=%-3d     %-8d  %-14s %-14s %-14s %-12s%s%n",
                n, k, RUNS,
                fmtNs(median), fmtNs(times[0]), fmtNs(times[RUNS-1]),
                fmtBytes(avgHeap), allOk ? "" : " ⚠ échec");
        }

        System.out.println();
        System.out.println("═".repeat(80));
        System.out.println("  MODÈLE THÉORIQUE : T = O(k^n · k·log k)   S = O(n·k)");
        System.out.println("  Surcoût principal : tri des candidats O(k log k) à chaque nœud.");
        System.out.println("  Dégradation exponentielle notable au-delà de n=20, k=5.");
        System.out.println("═".repeat(80));

        exportCsv(allResults, "benchmark_v2_results.csv");
        System.out.println("\n  Résultats bruts exportés → benchmark_v2_results.csv");
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
            fw.write("n,k,run,temps_ns,temps_ms,heap_bytes,heap_ko,success\n");
            for (BenchResult r : results)
                fw.write(String.format("%d,%d,%d,%d,%.4f,%d,%.2f,%s%n",
                    r.n, r.k, r.run, r.timeNs, r.timeNs / 1_000_000.0,
                    r.heapBytes, r.heapBytes / 1_024.0, r.success));
        } catch (IOException e) {
            System.err.println("Export CSV échoué : " + e.getMessage());
        }
    }
}
