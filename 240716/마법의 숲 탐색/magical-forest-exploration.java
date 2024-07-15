import java.io.*;
import java.util.*;

public class Main {
    static int R, C;
    static int K;
    static int[] columns; // 2 <= ci <= C-1
    static int[] directions;
    static int[][] map;

    static int answer;

    static final int max_R = 71;
    static final int max_C = 71;
    static final int max_K = 1001;
    // 1부터 시작
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

        StringTokenizer st = new StringTokenizer(br.readLine());

        R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        map = new int[R + 3][C + 1]; // 1 ~ R + 2, 1 ~ C
        columns = new int[K + 1];
        directions = new int[K + 1];
        for (int i = 1; i <= K; i++) {
            st = new StringTokenizer(br.readLine());

            columns[i] = Integer.parseInt(st.nextToken());
            directions[i] = Integer.parseInt(st.nextToken());
        }

        gollems = new Gollem[K + 1];
        isExit = new boolean[R + 3][C + 1];
        for (int i = 1; i <= K; i++) {
            gollems[i] = new Gollem(2,columns[i],directions[i]);
            games(i);
        }

        System.out.println(answer);


        bw.flush();
        br.close();
        bw.close();
    }
    static class Gollem{
        int centerX;
        int centerY;

        int exitDir;

        public Gollem(int centerX, int centerY, int exitDir) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.exitDir = exitDir;
        }
    }
    static Gollem[] gollems;
    private static void games(int idx) {
        gollemMove(idx);
        coloring(idx);
        if (!isRange(gollems[idx].centerX, gollems[idx].centerY)) {
            clean(); // 청소함.
            return; // count X
        }
        coloringExit(idx);
        fairyMove(idx);
    }

    private static void coloring(int idx) {
        int x = gollems[idx].centerX;
        int y = gollems[idx].centerY;

        map[x][y] = idx;

        for (int d = 0; d < 4; d++) {
            int nx = x + dx[d];
            int ny = y + dy[d];

            map[nx][ny] = idx;
        }
    }

    private static void fairyMove(int idx) {
        int x = gollems[idx].centerX;
        int y = gollems[idx].centerY;

        boolean[][] visited = new boolean[R + 3][C + 1];

        int num = map[x][y];

        Queue<int[]> que = new LinkedList<>();
        que.add(new int[]{x, y, num});
        visited[x][y] = true;

        int resultX = x;

        while (!que.isEmpty()) {
            int[] poll = que.poll();

            x = poll[0];
            y = poll[1];
            num = poll[2];

            for (int d = 0; d < 4; d++) { // 상 우 하 좌
                int nx = x + dx[d];
                int ny = y + dy[d];

                // 범위 밖이면 out
                if (!isRange(nx, ny)) continue;
                // 이미 방문한거면 out
                if (visited[nx][ny]) continue;
                // 이동 못하면 out
                if (map[nx][ny] == 0) continue; // 0 이면 못감

                int nnum = map[nx][ny];

                // exit이면 go ok
                // 새로운 좌표 색깔과 이전 색깔이 일치하거나
                // 색깔은 다른데 이전이 출구라면
                if (num == nnum || num != nnum && isExit[x][y]){
                    if (nx > resultX) { // 근데 그 좌표가 더 크다면
                        resultX = nx; // 새로 저장
                    }
                    // 큐에 넣음.
                    que.add(new int[]{nx, ny, nnum});
                    visited[nx][ny] = true;
                }
            }
        }

        answer += (resultX - 2); // 2칸씩 더해서 계산했으니 다시빼주기
    }

    private static boolean isRange(int nx, int ny) {
        return 3 <= nx && nx <= R + 2 && 1 <= ny && ny <= C;
    }

    static boolean[][] isExit;
    private static void coloringExit(int idx) {
        int x = gollems[idx].centerX;
        int y = gollems[idx].centerY;
        int d = gollems[idx].exitDir;

        int nx = x + dx[d];
        int ny = y + dy[d];

        isExit[nx][ny] = true;
    }

    private static void clean() {
        isExit = new boolean[R + 3][C + 1];
        for (int i = 1; i <= R + 2; i++) {
            for (int j = 1; j <= C; j++) {
                map[i][j] = 0;
            }
        }
    }

    // 2 ~ R + 1
    // 1 ~ C

    static int[] dx = {-1, 0, 1, 0}; // 상우하좌
    static int[] dy = {0, 1, 0, -1};
    private static void gollemMove(int idx) {
        int gollemRow = gollems[idx].centerX; // 중심 r : 2부터 시작
        int gollemColumn = gollems[idx].centerY; // 중심 c : 2부터 C - 1 사이
        int gollemDir = gollems[idx].exitDir;

        // 1. 남쪽, 서남쪽, 동남쪽
        int r = gollemRow;
        int c = gollemColumn;
        int d = gollemDir;

        //if (map[r + 1][c] != 0) return; // 이미 골렘이 있어서 들어갈수없음

        while (true) {
            // 남쪽
            if (centerRange(r + 2, c)
                    && map[r + 2][c] == 0
                    && map[r + 1][c - 1] == 0
                    && map[r + 1][c + 1] == 0) {
                r++; // 한칸 남쪽으로
            }
            // 서쪽
            else if (sideRange(r + 2, c - 2)
                    && map[r - 1][c - 1] == 0
                    && map[r][c - 2] == 0
                    && map[r + 1][c - 1] == 0
                    && map[r + 1][c - 2] == 0
                    && map[r + 2][c - 1] == 0) {
                r++;
                c--;
                // 출구수정 : 반시계방향
                d = (d + 3) % 4;
            } else if (sideRange(r + 2, c + 2)
                    && map[r - 1][c + 1] == 0
                    && map[r][c + 2] == 0
                    && map[r + 1][c + 1] == 0
                    && map[r + 1][c + 2] == 0
                    && map[r + 2][c + 1] == 0) {
                r++;
                c++;
                // 출구수정 : 시계방향
                d = (d + 1) % 4;
            }
            else{
                break;
            }
        }
        // 좌표 수정
        gollems[idx].centerX = r;
        gollems[idx].centerY = c;
        gollems[idx].exitDir = d;
    }

    private static boolean sideRange(int nr, int nc) {
        return 3 <= nr && nr <= R + 2 && 1 <= nc && nc <= C;
    }

    private static boolean centerRange(int r, int c) {
        return 4 <= r && r <= R + 1 && 2 <= c && c <= C - 1;
    }
}

/*

i번째 정령의 정보 : ci 골렘이 출발하는 열, di 골렘의 출구 방향 정보
4 <= gollemcenterX <= R + 1
2 <= gollemcenterY <= C - 1
끝이 1에있는경우 -1까지 가능

1. 골렘 이동
남쪽, 서남쪽, 동남쪽       1.
남쪽으로 이동못할경우 서쪽으로 이동후 남쪽으로 한칸이동.
출구는 시계반대방향으로 이동
동쪽으로 이동(시계방향으로 이동)

골렘일부가 숲을 벗어남
해당 골렘을 포함해 숲에 위치한 모든 골렘은 숲을 빠져나감.
다음 골렘부터 새롭게 이동한다.
단 이경우에는, 정령이 도달하는 최종위치를 답에 포함시키지 않는다.

숲이 다시 텅비어도, 여태 누적된 행의 총합은 계속 유지됨.

골렘의 위치정해짐
2. 정령 이동
상하좌우 인접한 칸으로 이동가능, 출구가 다른 골렘에 이어져있으면 다른 골렘으로 이동가능.
갈수있는 칸중 가장 남쪽으로 이동.
멈추면 거기가 최종위치가 됨.
result = 정령의 최종위치의 행번호의 합
최종 위치 누적해야함.

* */