import java.awt.*;
import java.io.*;
import java.util.*;

public class Main {
    static int N, M, K;
    static final int max_N = 11;
    static final int max_M = 11;
    static Tower[][] map;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken()); // 가로
        M = Integer.parseInt(st.nextToken()); // 세로
        K = Integer.parseInt(st.nextToken());

        map = new Tower[N + 1][M + 1];
        for (int i = 1; i <= N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 1; j <= M; j++) {
                map[i][j] = new Tower(i,j,Integer.parseInt(st.nextToken()), 0);
            }
        }

        for (int round = 1; round <= K; round++) {
            if (!isFinished()){
                game(round);
            }
        }

        int maxPower = 0;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (maxPower < map[i][j].power) {
                    maxPower = map[i][j].power;
                }
            }
        }

        System.out.println(maxPower);

        bw.flush();
        br.close();
        bw.close();
    }

    private static boolean isFinished() {
        int cnt = 0;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j].power > 0) {
                    cnt++;
                }
                if (cnt >= 2){
                    return false;
                }
            }
        }

        return true;
    }

    private static void game(int round) {
        selectAttacker(round);
        selectDefender();
        if (isFinished()) return;
        attack();
        broken();
        restore();
    }

    private static void broken() {
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j].power < 0) {
                    map[i][j].power = 0;
                }
            }
        }
    }

    private static void restore() {
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j].power > 0 && !isAttacked[i][j]) {
                    map[i][j].power++;
                }
            }
        }
    }

    private static void selectDefender() {
        int max = Integer.MIN_VALUE;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (i == lowTower.x && j == lowTower.y) {
                    continue;
                }
                if (map[i][j].power > 0) {
                    if (max < map[i][j].power) {
                        max = map[i][j].power;
                    }
                }
            }
        }

        // 최대값과 같은거 다 배열에 넣기
        ArrayList<Tower> maxTowers = new ArrayList<>();

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (max == map[i][j].power) {
                    maxTowers.add(map[i][j]);
                }
            }
        }

        // 대상 타워 기준
        // 1. 가장 높은값
        // 2. 가장 맞은지 오래된 탑
        // 3. 행과 열의 합이 가장 작은 탑
        // 4. 열의 값이 가장 작은 탑

        Collections.sort(maxTowers, new Comparator<Tower>() {
            @Override
            public int compare(Tower t1, Tower t2) {
                if (t1.round == t2.round) {
                    if ((t1.x + t1.y) == (t2.x + t2.y)) {
                        return t1.y - t2.y;
                    }
                    return (t1.x + t1.y) - (t2.x + t2.y);
                }
                return t1.round - t2.round;
            }
        });

        maxTower = maxTowers.get(0);
    }
    static Tower maxTower;

    static class Tower{
        int x;
        int y;

        int power;

        int round; // 턴 수

        public Tower(int x, int y, int power, int round) {
            this.x = x;
            this.y = y;
            this.power = power;
            this.round = round;
        }
    }

    static Tower lowTower;
    private static void selectAttacker(int round) {
        // 여러개일수있음.
        ArrayList<Tower> lowTowers = new ArrayList<>();
        // 최소값 찾기
        int min = Integer.MAX_VALUE;

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j].power > 0) {
                    if (min > map[i][j].power) {
                        min = map[i][j].power;
                    }
                }
            }
        }

        // 최소값이랑 같은거 모조로 list에 넣기
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (min == map[i][j].power) {
                    lowTowers.add(map[i][j]);
                }
            }
        }

        // 1. 최소값 끝
        // 2. 가장 최근에 공격
        // 3. 행과 열의 합이 가장 큰것
        // 4. 열이 가장 큰것

        Collections.sort(lowTowers, new Comparator<Tower>() {
            @Override
            public int compare(Tower t1, Tower t2) {
                if (t2.round != t1.round) return t2.round - t1.round;
                if ((t2.x + t2.y) != (t1.x + t1.y)) return (t2.x + t2.y) - (t1.x + t1.y);
                return t2.y - t1.y;
            }
        });

        lowTower = lowTowers.get(0);

        // round 수정
        lowTower.round = round;
        lowTower.power += (N + M);

        map[lowTower.x][lowTower.y].round = lowTower.round;
        map[lowTower.x][lowTower.y].power = lowTower.power;
    }

    private static void attack() {
        if (hasRoute()) {
            razer();
        }
        else{
            bomb();
        }
    }

    static int[] ddx = {-1, 1, 0, 0, 1, 1, -1, -1};
    static int[] ddy = {0, 0, -1, 1, -1, 1, 1, -1};
    private static void bomb() {
        int ex = maxTower.x; // 목표 대상 x, y 좌표
        int ey = maxTower.y;

        // power 수정
        maxTower.power -= lowTower.power;
        map[ex][ey].power = maxTower.power;

        isAttacked = new boolean[N + 1][M + 1];
        isAttacked[lowTower.x][lowTower.y] = true;
        isAttacked[ex][ey] = true;

        // 좌표를 넘어갈때 +N을 더하고 %N을 해서 비율을 맞추는거같음

        for (int d = 0; d < 8; d++) {
            int nx = ex - 1; // x - 1
            nx = (nx + ddx[d] + N) % N; // nx계산
            nx = nx + 1; // nx + 1

            int ny = ey - 1;
            ny = (ny + ddy[d] + M) % M;
            ny = ny + 1;

            if (nx == lowTower.x && ny == lowTower.y) {
                // 공격대상이면 제외
                continue;
            }

            map[nx][ny].power -= lowTower.power / 2;
            isAttacked[nx][ny] = true;
        }
    }

    private static void razer() {
        int sx = lowTower.x;
        int sy = lowTower.y;

        int ex = maxTower.x;
        int ey = maxTower.y;

        // power 값 바꾸기
        maxTower.power -= lowTower.power;
        map[maxTower.x][maxTower.y].power = maxTower.power;

        isAttacked = new boolean[N + 1][M + 1];
        isAttacked[lowTower.x][lowTower.y] = true;
        isAttacked[maxTower.x][maxTower.y] = true;

        int nx = backTower[ex][ey].x;
        int ny = backTower[ex][ey].y;

        ex = nx;
        ey = ny;

        while (!(sx == ex && sy == ey)) {
            map[ex][ey].power -= lowTower.power/2;
            isAttacked[ex][ey] = true;

            // 좌표가 바뀐채로 담기니까 그렇지 등신아.....
            nx = backTower[ex][ey].x;
            ny = backTower[ex][ey].y;

            ex = nx;
            ey = ny;
        }
    }

    static boolean[][] isAttacked;

    // bfs, 경로 저장
    static int[] dx = {0, 1, 0, -1}; // 우하좌상
    static int[] dy = {1, 0, -1, 0};
    static Tower[][] backTower;
    private static boolean hasRoute() {
        Queue<Tower> que = new LinkedList<>();
        boolean[][] visited = new boolean[N + 1][M + 1];
        que.add(lowTower);
        visited[lowTower.x][lowTower.y] = true;
        backTower = new Tower[N + 1][M + 1];

        while (!que.isEmpty()) {
            Tower poll = que.poll();

            if (poll.x == maxTower.x && poll.y == maxTower.y) {
                return true;
            }

            for (int d = 0; d < 4; d++) {
                int nx = poll.x - 1;
                int ny = poll.y - 1; // 평행이동

                nx = (nx + dx[d] + N) % N;
                ny = (ny + dy[d] + M) % M;

                nx = nx + 1;
                ny = ny + 1;

                // 방문함
                if (visited[nx][ny]) continue;
                // 벽임
                if (map[nx][ny].power == 0) continue;

                // 이동가능
                que.add(map[nx][ny]);
                visited[nx][ny] = true;
                // 이동경로는 어케저장함.... -> 역추적하는 배열을 저장함
                backTower[nx][ny] = map[poll.x][poll.y];
            }
        }
        return false;
    }
}