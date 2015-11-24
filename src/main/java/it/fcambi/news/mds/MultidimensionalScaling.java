package it.fcambi.news.mds;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by Francesco on 17/11/15.
 */
public class MultidimensionalScaling {

    /**
     *
     * @param distances between each point
     * @return points Matrix with n rows (one for each point)
     *         and 2 columns (one for dimension) where points[i] = (x,y)
     */
    public static double[][] exec(double[][] distances) {

        //nxn matrix
        RealMatrix D = MatrixUtils.createRealMatrix(distances);

        if (!D.isSquare())
            throw new IllegalArgumentException("Invalid (non-square) input matrix.");
        int n = D.getRowDimension();

        //Compute D squared
        for (int i=0; i < n; i++)
            for (int j=0; j < n; j++)
                D.multiplyEntry(i, j, D.getEntry(i, j));

        //Double centering
        //J = I-n^-1 1 1'
        RealMatrix id = MatrixUtils.createRealIdentityMatrix(n);
        RealMatrix ones = MatrixUtils.createRealMatrix(n, n).scalarAdd(1.0/n);
        RealMatrix J = id.subtract(ones);

        //B = -1/2 J D^2 J
        RealMatrix B = J.multiply(D).multiply(J).scalarMultiply(-0.5);

        //Compute eigenvalues and eigenvectors of B
        EigenDecomposition eig = new EigenDecomposition(B);
        //Choose higher eigenvalues and associated eigenvectors
        double eig_a = -1.0;
        int eig_a_idx = -1;
        double eig_b = -1.0;
        int eig_b_idx = -1;

        for (int i=0; i < n; i++) {
            double l = eig.getD().getEntry(i, i);
            if (l >= 0) {
                if (l > eig_a) {
                    eig_a = l;
                    eig_a_idx = i;
                } else if (l > eig_b) {
                    eig_b = l;
                    eig_b_idx = i;
                }
            }
        }

        RealMatrix Em = MatrixUtils.createRealMatrix(n, 2);
        Em.setColumnVector(0, eig.getV().getColumnVector(eig_a_idx));
        Em.setColumnVector(1, eig.getV().getColumnVector(eig_b_idx));

        double[] eigenvalues = {Math.sqrt(eig_a), Math.sqrt(eig_b)};
        RealMatrix Lm = MatrixUtils.createRealDiagonalMatrix(eigenvalues);

        RealMatrix points = Em.multiply(Lm);

        return points.getData();

    }

//    public static void printMatrix(RealMatrix a) {
//        for (int i=0; i < a.getRowDimension(); i++) {
//            for (int j = 0; j < a.getColumnDimension(); j++)
//                System.out.print(a.getEntry(i, j)+"\t");
//            System.out.print("\n");
//        }
//    }

}
