package Civil.Sharif.Fathi.drivingeventlabeler.activity;

public class DTW {

    protected float[] seq1;
    protected float[] seq2;
    protected int[][] warpingPath;

    protected int n;
    protected int m;
    protected int K;

    protected float warpingDistance;

    /**
     * Constructor
     *
     * @param sample
     * @param templete
     */
    public DTW(float[] sample, float[] templete) {
        seq1 = sample;
        seq2 = templete;

        n = seq1.length;
        m = seq2.length;
        K = 1;

        warpingPath = new int[n + m][2];    // max(n, m) <= K < n + m
        warpingDistance = 0;

        this.compute();
    }

    public void clear() {
            seq1 = null;
            seq2 = null;
            warpingPath = null;
            n = 0;
            m = 0;
            K = 0;
            warpingDistance = 0;

            System.gc();
            Runtime.getRuntime().freeMemory();
            }


    public void compute() {
            float accumulatedDistance = 0;

            float[][] d = new float[n][m];  // local distances
            float[][] D = new float[n][m];  // global distances

            for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
            d[i][j] = distanceBetween(seq1[i], seq2[j]);
            }
            }

            D[0][0] = d[0][0];

            for (int i = 1; i < n; i++) {
            D[i][0] = d[i][0] + D[i - 1][0];
            }

            for (int j = 1; j < m; j++) {
            D[0][j] = d[0][j] + D[0][j - 1];
            }

            for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
            accumulatedDistance = Math.min(Math.min(D[i-1][j], D[i-1][j-1]), D[i][j-1]);
            accumulatedDistance += d[i][j];
            D[i][j] = accumulatedDistance;
            }
            }
            accumulatedDistance = D[n - 1][m - 1];

            int i = n - 1;
            int j = m - 1;
            int minIndex = 1;

            warpingPath[K - 1][0] = i;
            warpingPath[K - 1][1] = j;

            while ((i + j) != 0) {
            if (i == 0) {
            j -= 1;
            } else if (j == 0) {
            i -= 1;
            } else {    // i != 0 && j != 0
            float[] array = { D[i - 1][j], D[i][j - 1], D[i - 1][j - 1] };
            minIndex = this.getIndexOfMinimum(array);

            if (minIndex == 0) {
            i -= 1;
            } else if (minIndex == 1) {
            j -= 1;
            } else if (minIndex == 2) {
            i -= 1;
            j -= 1;
            }
            } // end else
            K++;
            warpingPath[K - 1][0] = i;
            warpingPath[K - 1][1] = j;
            } // end while
            warpingDistance = accumulatedDistance / K;

            //this.reversePath(warpingPath);

            //Clear
//            this.clear();
            //d = null;
            //D = null;
            //warpingPath = null;
            //accumulatedDistance = 0;
            }

    /**
     * Changes the order of the warping path (increasing order)
     *
     * @param path  the warping path in reverse order
     */

            /*
        protected void reversePath(int[][] path) {
            int[][] newPath = new int[K][2];
            for (int i = 0; i < K; i++) {
                for (int j = 0; j < 2; j++) {
                    newPath[i][j] = path[K - i - 1][j];
                }
            }
            warpingPath = newPath;
        }
            */

    /**
     * Returns the warping distance
     *
     * @return
     */
    public float getDistance() {
            return warpingDistance;
            }

    /**
     * Computes a distance between two points
     *
     * @param p1    the point 1
     * @param p2    the point 2
     * @return      the distance between two points
     */
    protected float distanceBetween(float p1, float p2) {
            return Math.abs(p1 - p2);
            }

    /**
     * Finds the index of the minimum element from the given array
     *
     * @param array     the array containing numeric values
     * @return              the min value among elements
     */
    protected int getIndexOfMinimum(float[] array) {
            int index = 0;
            float val = array[0];

            for (int i = 1; i < array.length; i++) {
            if (array[i] < val) {
            val = array[i];
            index = i;
            }
            }
            return index;
            }

    /**
     *  Returns a string that displays the warping distance and path
     */
    public String toString() {
            String retVal = "Warping Distance: " + warpingDistance + "\n";
                    /*
            retVal += "Warping Path: {";
            for (int i = 0; i < K; i++) {
                retVal += "(" + warpingPath[i][0] + ", " +warpingPath[i][1] + ")";
                retVal += (i == K - 1) ? "}" : ", ";

            }
                    */
            return retVal;
            }

    /**
     * Tests this class
     *
     * @param args  ignored
     */
    public static void main(String[] args) {
            float[] n2 = {1, 2, 3, 4, 5, 9, 19, 49.7555f};
            float[] n1 = {1, 2, 3, 4};
            DTW dtw = new DTW(n1, n2);
            System.out.println(dtw);
    }
}