package com.chatea.voxeleditor;

public class Utils {

    public static void vectorAdd(float[] result, float[] a, float[] b) {
        if (result.length != a.length || result.length != b.length) {
            throw new IllegalArgumentException("size of vector are different");
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = a[i] + b[i];
        }
    }

    public static void vectorSub(float[] result, float[] a, float[] b) {
        if (result.length != a.length || result.length != b.length) {
            throw new IllegalArgumentException("size of vector are different");
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = a[i] - b[i];
        }
    }

    public static void vectorFactor(float[] result, float factor, float[] a) {
        if (result.length != a.length) {
            throw new IllegalArgumentException("size of vector are different");
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = factor * a[i];
        }
    }

    public static float vectorDot(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("size of dot vector are different");
        }

        float result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    public static float[] calculateProjectToPointVector(float[] start, float[] line, float[] pointA) {
        if (start.length != line.length || start.length != pointA.length) {
            throw new IllegalArgumentException("size of vector are different");
        }
        float[] vectorPA = new float[3];
        float[] vectorOA = new float[3];
        float[] vectorOP = new float[3];

        vectorSub(vectorOA, pointA, start);

        float factor = vectorDot(vectorOA, line) / vectorDot(line, line);

        vectorFactor(vectorOP, factor, line);
        vectorSub(vectorPA, vectorOA, vectorOP);

        return vectorPA;
    }

    public static float[] getLineTriangleCrossPoint(float[] start, float[] line, float[] pointA, float[] pointB, float[] pointC) {
        if (start.length != line.length || start.length != pointA.length
                || start.length != pointB.length || start.length != pointC.length) {
            throw new IllegalArgumentException("size of vector are different");
        }
        // find a plan orthogonal to line and make the across point as origin.
        // Then project the pointA, pointB, pointC on the plan.
        float[] projectedA = calculateProjectToPointVector(start, line, pointA);
        float[] projectedB = calculateProjectToPointVector(start, line, pointB);
        float[] projectedC = calculateProjectToPointVector(start, line, pointC);

        float[] vectorAO = new float[3];
        float[] vectorAB = new float[3];
        float[] vectorAC = new float[3];

        vectorFactor(vectorAO, -1, projectedA);
        vectorSub(vectorAB, projectedB, projectedA);
        vectorSub(vectorAC, projectedC, projectedA);

        // if " AO = m * AB + n * AC, m >= 0, n >= 0, m + n <= 1", the line is pass triangle.

        // use 2*2 array to calculate m & n.
        //  [ ABx ACx ] [m]   [ AOx ]
        //  [ ABy ACx ] [n] = [ AOy ]

        float determinant = vectorAB[0] * vectorAC[1] - vectorAB[1] * vectorAC[0];
        if (determinant == 0) {
            // AB and AC are in the same/oppose direction.
            // what we selected is looks like a line, don't select it.
            return null;
        }

        float m = ( vectorAC[1] * vectorAO[0] - vectorAC[0] * vectorAO[1]) / determinant;
        float n = (-vectorAB[1] * vectorAO[0] + vectorAB[0] * vectorAO[1]) / determinant;

        float deviation = 0.1f;
        if (Math.abs(m * vectorAB[2] + n * vectorAC[2] - vectorAO[2]) < Math.abs(vectorAO[2] * deviation)) {
            if (n >= 0 && m >= 0 && m + n <= 1) { // FIXME? how about the deviation?
                // if either n or m is 0, the click point is on edge.

                float[] originAB = new float[3];
                float[] originAC = new float[3];
                vectorSub(originAB, pointB, pointA);
                vectorSub(originAC, pointC, pointA);

                float[] crossPoint = new float[3];
                float[] temp = new float[3];

                vectorFactor(temp, m, originAB);
                vectorAdd(crossPoint, pointA, temp);
                vectorFactor(temp, n, originAC);
                vectorAdd(crossPoint, crossPoint, temp);

                return crossPoint;
            }
        }

        return null;
    }

    public static float getDistanceSquare(float[] pointA, float[] pointB) {
        if (pointA.length != pointB.length) {
            throw new IllegalArgumentException("size of vector are different");
        }

        float result = 0;
        for (int i = 0; i < pointA.length; i++) {
            float temp = pointB[i] - pointA[i];
            result += temp * temp;
        }
        return result;
    }
}

