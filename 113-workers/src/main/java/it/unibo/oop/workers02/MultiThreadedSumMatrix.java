package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of SumMatrix.
 */
public class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthread;

    /**
     * Constructor.
     * 
     * @param nthread number of thread
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double sum(final double[][] matrix) {
        final List<Double> flatList = Arrays.stream(matrix)
            .flatMapToDouble(Arrays::stream)
            .boxed()
            .toList();

        final int size = flatList.size() % this.nthread + flatList.size() / this.nthread;

        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < flatList.size(); start += size) {
            int nelem = size;
            if (start + nelem > flatList.size()) {
                nelem = flatList.size() - start;
            } 
            workers.add(new Worker(flatList, start, nelem));
        } 

        for (final Worker w: workers) {
            w.start();
        }

        double sum = 0.0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }

    private static class Worker extends Thread {
        private final List<Double> listOfMatrix;
        private final int startpos;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         *
         * @param listOfMatrix
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final List<Double> listOfMatrix, final int startpos, final int nelem) {
            super();
            this.listOfMatrix = listOfMatrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            for (int i = startpos; i < listOfMatrix.size() && i < startpos + nelem; i++) {
                this.res += this.listOfMatrix.get(i);
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         *
         * @return the sum of every element in the array
         */
        public synchronized double getResult() {
            return this.res;
        }

    }

}
