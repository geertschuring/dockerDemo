package nl.juniverse.demo;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

/**
 * Generates Load on the CPU by keeping it busy for the given load percentage
 */
public class SystemLoad {

    /**
     * Starts the Load Generation
     * 
     * @param args
     *            Command line arguments, ignored
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        double load = 1;
        final long duration = 5000;

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        System.out.println("Checking Processor...");
        printProcessor(hal.getProcessor());

        System.out.println("Checking Memory...");
        printMemory(hal.getMemory());

        System.out.println("Checking File System...");
        printFileSystem(os.getFileSystem());

        System.out.println();
        System.out.println("Starting worker threads...");
        for (int thread = 0; thread < hal.getProcessor().getLogicalProcessorCount(); thread++) {
            new BusyThread("Thread" + thread, load, duration).start();
            Thread.sleep(1); // allow the threads to start in proper order.
        }

        Thread.sleep(duration);
        System.out.println("Done!");
    }

    private static void printProcessor(CentralProcessor processor) {
        System.out.println(" " + processor.getPhysicalProcessorCount() + " physical CPU core(s)");
        System.out.println(" " + processor.getLogicalProcessorCount() + " logical CPU(s)");
    }

    private static void printMemory(GlobalMemory memory) {
        System.out.println(" Memory: " + FormatUtil.formatBytes(memory.getAvailable()) + "/" + FormatUtil.formatBytes(memory.getTotal()));
        System.out.println(" Swap used: " + FormatUtil.formatBytes(memory.getSwapUsed()) + "/" + FormatUtil.formatBytes(memory.getSwapTotal()));
    }

    private static void printFileSystem(FileSystem fileSystem) {
        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            System.out.format(
                    " %s (%s) [%s] %s of %s free (%.1f%%) is %s "
                            + (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s")
                            + " and is mounted at %s%n",
                    fs.getName(),
                    fs.getDescription().isEmpty() ? "file system" : fs.getDescription(),
                    fs.getType(),
                    FormatUtil.formatBytes(usable),
                    FormatUtil.formatBytes(fs.getTotalSpace()),
                    100d * usable / total,
                    fs.getVolume(),
                    fs.getLogicalVolume(),
                    fs.getMount());
        }
    }

    private static class BusyThread extends Thread {

        private double load;

        private long duration;

        public BusyThread(String name, double load, long duration) {
            super(name);
            this.load = load;
            this.duration = duration;
        }

        @Override
        public void run() {
            System.out.println(" Started " + getName());
            long startTime = System.currentTimeMillis();
            try {
                // Loop for the given duration
                while (System.currentTimeMillis() - startTime < duration) {
                    // Every 100ms, sleep for the percentage of unladen time
                    if (System.currentTimeMillis() % 100 == 0) {
                        Thread.sleep((long) Math.floor((1 - load) * 100));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
