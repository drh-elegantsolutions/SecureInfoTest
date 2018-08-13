import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// you can also use imports, for example:
// import java.util.*;

// you can write to stdout for debugging purposes, e.g.
// System.out.println("this is a debug message");

class Solution {
    public int solution(int N, String S) {
        int count = 0;

        Map<Integer, List<Character>> seatMap = splitOccupiedSeats(S);
        for(int index = 1; index <= N; index++) {
            List<Character> occupiedSeats = seatMap.get(index);
            if(occupiedSeats == null) {
                occupiedSeats = new ArrayList<>();
            }
            int rowCount = rowCount(occupiedSeats);
            count += rowCount;
        }
        return count;
    }

    private int rowCount(List<Character> occupiedSeats) {
        int total = 0;

        if(countOccupied(occupiedSeats, 'A', 'B', 'C') == 0) {
            total += 1;
        }
        if(countOccupied(occupiedSeats, 'D', 'E', 'F', 'G') < 2
        && occupiedSeats.contains('E') == false
        && occupiedSeats.contains('F') == false) {
            total += 1;
        }
        if(countOccupied(occupiedSeats, 'H', 'J', 'K') == 0) {
            total += 1;
        }

        return total;
    }

    private int countOccupied(List<Character> occupiedSeats, char...seats) {
        int occupied = 0;
        for(char seat: seats) {
            if(occupiedSeats.contains(Character.valueOf(seat))) {
                occupied++;
            }
        }

        return occupied;
    }

    private Map<Integer, List<Character>> splitOccupiedSeats(String occupiedSeats) {
        Map<Integer, List<Character>> map = new HashMap<>();
        if(occupiedSeats != null) {
            String[] seats = occupiedSeats.split(" ");
            for(String seat: seats) {
                seat = seat.trim();
                if("".equals(seat) == false) {
                    // skipping format validation (assumed to be ok)
                    char column = seat.charAt(seat.length() - 1);
                    String row = seat.substring(0, seat.length() - 1);
                    Integer rowInt = Integer.valueOf(row);

                    List<Character> rowList = map.get(rowInt);
                    if(rowList == null) {
                        rowList = new ArrayList<>();
                        map.put(rowInt, rowList);
                    }

                    rowList.add(Character.valueOf(column));
                }
            }
        }
        return map;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();

        int N = 10;
        String S = "10A 10F";
        //(1, '')
        int count = solution.solution(N, S);
        System.out.println("count = " + count);
    }
}