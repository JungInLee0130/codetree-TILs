import java.awt.*;
import java.io.*;
import java.util.*;

public class Main {
    static int K, M;

    static int[][] map;

    static final int max_N = 5; // 0 ~ 4까지만 사용한다.
    static final int max_M = 300; // 0 ~ 299까지
    static final int max_K = 11; // 1 ~ 10까지

    static int[] wallNum;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

        StringTokenizer st = new StringTokenizer(br.readLine());

        K = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());

        map = new int[max_N][max_N];
        for (int i = 0; i < 5; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < 5; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        wallNum = new int[M];
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < M; i++) {
            wallNum[i] = Integer.parseInt(st.nextToken());
        }

        for (int round = 1; round <= K; round++) {
            int value = game(round);
            if (value == 0) {
                break;
            }
            System.out.print(value + " ");
        }

        bw.flush();
        br.close();
        bw.close();
    }

    static int totalValue = 0;
    static boolean isFinish = false;
    private static int game(int round) {
        int value = explore(); // rotate
        if (value == 0) {
            isFinish = true;
            return 0;
        }
        clear();
        while (true) {
            putWallNum();
            int v = gain(); // get
            if (v == 0) break;
            value += v;
        }

        return value;
    }

    private static int gain() {
        // value는 더하고, 지움.
        boolean[][] isVisited = new boolean[5][5];
        int value = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if(isVisited[i][j]) continue;
                value += bfs(i,j, isVisited, false, 0);
            }
        }
        return value;
    }

    static int wallIdx = 0;
    private static void putWallNum() {
        for (int j = 0; j <= 4; j++) {
            for (int i = 4; i >= 0; i--) {
                if (map[i][j] == 0) {
                    map[i][j] = wallNum[wallIdx];
                    wallIdx++;
                }
            }
        }
    }

    private static void clear() {
        // 진짜 맵에서 같은거 싹지움.
        boolean[][] isVisited = new boolean[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if(isVisited[i][j]) continue;
                bfs(i,j, isVisited, true, 0);
            }
        }
    }

    private static int bfs(int startX, int startY, boolean[][] isVisited
            , boolean isClear, int value) {
        Queue<int[]> que = new LinkedList<>();
        Queue<int[]> lastQue = new LinkedList<>();
        isVisited[startX][startY] = true;
        int num = map[startX][startY];
        que.add(new int[]{startX, startY, num});
        lastQue.add(new int[]{startX, startY, num});

        while (!que.isEmpty()) {
            int[] poll = que.poll();

            for (int d = 0; d < 4; d++) {
                int nx = poll[0] + dx[d];
                int ny = poll[1] + dy[d];

                if (!isRange(nx,ny)) continue;
                int nnum = map[nx][ny];
                if (isVisited[nx][ny]) continue;
                if (nnum != num) continue;

                isVisited[nx][ny] = true;
                que.add(new int[]{nx, ny, nnum});
                lastQue.add(new int[]{nx, ny, nnum});
            }
        }

        if (lastQue.size() >= 3) {
            if (isClear) {
                while (!lastQue.isEmpty()) {
                    int[] poll = lastQue.poll();
                    map[poll[0]][poll[1]] = 0;
                }
                return 0;
            }
            else{
                value = lastQue.size();
                while (!lastQue.isEmpty()) {
                    int[] poll = lastQue.poll();
                    map[poll[0]][poll[1]] = 0;
                }
                return value;
            }
        }
        return 0;
    }

    private static int explore() {
        // 1. 유물 1차획득이 최대
        // 2. 회전한 각도가 작은순
        // 3. 중심좌표 열이 작은순
        // 4. 중심좌표 행이 작은순

        // 0 ~ 4
        maxValue = 0;
        maxValueCenterPoint = new int[]{1, 1, 0};
        int[][] copyMap = new int[5][5];
        copyOriginal(map, copyMap);
        maxValueMap = new int[5][5]; // maxValueMap Copy

        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                rotate(copyMap,i,j);
            }
        }

        copyOriginal(maxValueMap, map);

        return maxValue;
    }
    static int maxValue;
    static int[] maxValueCenterPoint;
    static int[][] maxValueMap;
    private static void rotate(int[][] copyMap, int centerX, int centerY) {
        // 1,1로 평행이동
        int rx = centerX - 1;
        int ry = centerY - 1;

        int[][] oMap = new int[3][3];
        copyMap(oMap, centerX, centerY);
        int[][] cMap = new int[3][3];

        // 90도
        rotateMap(cMap, oMap);
        copyOriginal(map, copyMap);
        copyMap(copyMap, cMap, rx, ry);
        int value = getValue(copyMap);
        if (maxValue < value || maxValue == value && maxValueCenterPoint[2] > 90) {
            maxValue = value;
            maxValueCenterPoint = new int[]{centerX, centerY, 90};
            copyOriginal(copyMap, maxValueMap);
        }

        // 180도
        int[][] cMap2 = new int[3][3];
        rotateMap(cMap2, cMap);
        copyOriginal(map, copyMap);
        copyMap(copyMap, cMap2, rx, ry);
        value = getValue(copyMap);
        if (maxValue < value || maxValue == value && maxValueCenterPoint[2] > 180) {
            maxValue = value;
            maxValueCenterPoint = new int[]{centerX, centerY, 180};
            copyOriginal(copyMap, maxValueMap);
        }

        // 270도
        int[][] cMap3 = new int[3][3];
        rotateMap(cMap3, cMap2);
        copyOriginal(map, copyMap);
        copyMap(copyMap, cMap3, rx, ry);
        value = getValue(copyMap);
        if (maxValue < value || maxValue == value && maxValueCenterPoint[2] > 270) {
            maxValue = value;
            maxValueCenterPoint = new int[]{centerX, centerY, 270};
            copyOriginal(copyMap, maxValueMap);
        }
    }

    private static int getValue(int[][] copyMap) {
        boolean[][] isVisited = new boolean[5][5];
        int totalPoint = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (isVisited[i][j]) continue;
                totalPoint += bfs(copyMap, i, j, isVisited);
            }
        }
        return totalPoint;
    }

    static int[] dx = {-1, 1, 0, 0};
    static int[] dy = {0, 0, -1, 1};
    private static int bfs(int[][] copyMap, int startX, int startY, boolean[][] isVisited) {
        Queue<int[]> que = new LinkedList<>();
        int num = copyMap[startX][startY];
        isVisited[startX][startY] = true;
        que.add(new int[]{startX, startY, num});

        int count = 1;
        while (!que.isEmpty()) {
            int[] poll = que.poll();

            for (int d = 0; d < 4; d++) {
                int nx = poll[0] + dx[d];
                int ny = poll[1] + dy[d];

                if (!isRange(nx,ny)) continue;
                int nnum = copyMap[nx][ny];
                if (isVisited[nx][ny]) continue;
                if (nnum != poll[2]) continue;

                count++;
                isVisited[nx][ny] = true;
                que.add(new int[]{nx, ny, nnum});
            }
        }

        if (count >= 3) {
            return count;
        }

        return 0;
    }

    private static boolean isRange(int nx, int ny) {
        return 0 <= nx && nx <= 4 && 0 <= ny && ny <= 4;
    }

    private static void copyMap(int[][] copyMap, int[][] cMap, int rx, int ry) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                copyMap[i + rx][j + ry] = cMap[i][j];
            }
        }
    }

    private static void rotateMap(int[][] cMap, int[][] map) {
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                cMap[j][2 - i] = map[i][j];
            }
        }
    }

    private static void copyMap(int[][] oMap, int centerX, int centerY) {
        int x = centerX - 1;
        int y = centerY - 1;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                oMap[i][j] = map[x + i][y + j];
            }
        }
    }

    private static void copyOriginal(int[][] map, int[][] copyMap) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                copyMap[i][j] = map[i][j];
            }
        }
    }
}