import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// you can also use imports, for example:
// import java.util.*;

// you can write to stdout for debugging purposes, e.g.
// System.out.println("this is a debug message");

class Solution1 {
    // this is fast, though a little verbose, but i cannot prove it is correct, though i think it is
//    public String solution(int A, int B, int C, int D) {
//        validate(A, "A");
//        validate(B, "B");
//        validate(C, "C");
//        validate(D, "D");
//
//        // can't use calendar.set as it ignores lenient setting
//        // SimpleDateFormat is a bit heavyweight for such a simple comparison
//
//        // a bit verbose, but should be v.fast
//        // (benefits from each digit max range is less than the next digits max range)
//        // calculate one digit at a time
//        List<Integer> list = new ArrayList<>();
//        list.add(A);
//        list.add(B);
//        list.add(C);
//        list.add(D);
//
//        Collections.sort(list);
//        Collections.reverse(list);
//
//        int[] result = {-1, -1, -1, -1};
//
//        // first digit
//        for(int index = 0; index < list.size(); index++) {
//            int value = list.get(index);
//            int timeValue = value * 10;
//            if(timeValue < 24) {
//                list.remove(index);
//                result[0] = value;
//                break;
//            }
//        }
//        if(result[0] == -1) {
//            return "NOT POSSIBLE";
//        }
//
//        // second digit
//        for(int index = 0; index < list.size(); index++) {
//            int value = list.get(index);
//            int timeValue = (result[0] * 10) + value;
//            if(timeValue < 24) {
//                list.remove(index);
//                result[1] = value;
//                break;
//            }
//        }
//        if(result[1] == -1) {
//            return "NOT POSSIBLE";
//        }
//
//        // third digit
//        for(int index = 0; index < list.size(); index++) {
//            int value = list.get(index);
//            int timeValue = value * 10;
//            if(timeValue < 60) {
//                list.remove(index);
//                result[2] = value;
//                break;
//            }
//        }
//        if(result[2] == -1) {
//            return "NOT POSSIBLE";
//        }
//
//        // fourth digit
//        int value = list.get(0);
//        int timeValue = result[2] * 10 + value;
//        if(timeValue < 60) {
//            result[3] = value;
//        } else {
//            return "NOT POSSIBLE";
//        }
//
//        return String.format("%d%d:%d%d", result[0], result[1], result[2], result[3]);
//    }


    public String solution(int A, int B, int C, int D) {
        validate(A, "A");
        validate(B, "B");
        validate(C, "C");
        validate(D, "D");

        List<Integer> values = new ArrayList<>();
        values.add(A);
        values.add(B);
        values.add(C);
        values.add(D);

        List<Integer> maxTimes = new ArrayList<>();
        maxTimes.add(-1);
        maxTimes.add(-1);
        maxTimes.add(-1);
        maxTimes.add(-1);

        permute(maxTimes, values, 0);

        if(maxTimes.get(0) == -1 || maxTimes.get(1) == -1 || maxTimes.get(2) == -1 || maxTimes.get(3) == -1) {
            return "NOT POSSIBLE";
        }
        return String.format("%d%d:%d%d", maxTimes.get(0), maxTimes.get(1), maxTimes.get(02), maxTimes.get(3));
    }

    // based on http://stackoverflow.com/questions/2920315/permutation-of-array
    private void permute(List<Integer> maxTime, List<Integer> values, int position){
        for(int index = position; index < values.size(); index++){
            Collections.swap(values, index, position);
            permute(maxTime, values, position+1);
            Collections.swap(values, position, index);
        }

        if (position == values.size() -1) {
//            System.out.println(java.util.Arrays.toString(values.toArray()) + "  = " + score(values));
            // assess permutation
            if(score(maxTime) < score(values)) {
//                System.out.println("new maxTime");
                maxTime.set(0, values.get(0));
                maxTime.set(1, values.get(1));
                maxTime.set(2, values.get(2));
                maxTime.set(3, values.get(3));
            }
        }
    }

    private int score(List<Integer> values) {
        if(values.get(0) > 2 || values.get(0) < 0) {
            return -1;
        }
        if((values.get(0) == 2 && values.get(1) > 3)
         || values.get(1) > 9
         || values.get(1) < 0) {
            return -1;
        }
        if(values.get(2) > 5 || values.get(2) < 0) {
            return -1;
        }
        if(values.get(3) > 9 || values.get(3) < 0) {
            return -1;
        }

        int score = 0;
        for(Integer value: values) {
            score *= 10;
            score += (value == null ? 0 : value.intValue());
        }

        return score;
    }

    private void validate(int value, String displayName) {
        if(value < 0) {
            throw new IllegalArgumentException("" + displayName + " cannot be less than zero");
        }
        if(value > 9) {
            throw new IllegalArgumentException("" + displayName + " cannot be more than nine");
        }
    }

    public static void main(String[] args) {
        // not exhaustive ...
        int[][] testValues = {
                {2, 3, 1, 8},
                {1, 8, 3, 2},
                {8, 3, 2, 1},
                {2, 3, 5, 9},
                {2, 3, 6, 0},
                {2, 3, 6, 6},
                {0, 0, 0, 0},
                {9, 0, 0, 0},
                {3, 0, 7, 0}
        };

        for(int index = 0; index < testValues.length; index++) {
            Solution1 solution = new Solution1();
            int[] values = testValues[index];
            System.out.println(solution.solution(values[0], values[1], values[2], values[3]));
        }
    }
}