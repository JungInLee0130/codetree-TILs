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
        selectDefender(round);
        attack(lowTower, maxTower);
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

    private static void selectDefender(int round) {
        int max = Integer.MIN_VALUE;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (max < map[i][j].power) {
                    max = map[i][j].power;
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
                if (Integer.compare(t1.round, t2.round) == 0) {
                    if (Integer.compare(t1.x + t1.y, t2.x + t2.y) == 0) {
                        return Integer.compare(t1.y, t2.y);
                    }
                    return Integer.compare(t1.x + t1.y, t2.x + t2.y);
                }
                return Integer.compare(t1.round, t2.round);
            }
        });

        maxTower = maxTowers.get(0);

        // 수정할께 없음.
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
                if (Integer.compare(t2.round, t1.round) == 0) {
                    if (Integer.compare(t2.x + t2.y, t1.x + t1.y) == 0) {
                        return Integer.compare(t2.y, t1.y);
                    }
                    return Integer.compare(t2.x + t2.y, t1.x + t1.y);
                }
                return Integer.compare(t2.round, t1.round);
            }
        });

        lowTower = lowTowers.get(0);

        // round 수정
        lowTower.round = round;
        lowTower.power += N + M;
        map[lowTower.x][lowTower.y].round = lowTower.round;
        map[lowTower.x][lowTower.y].power = lowTower.power;
    }

    private static void attack(Tower minTower, Tower maxTower) {
        if (hasRoute(minTower, maxTower)) {
            razer(minTower, maxTower);
        }
        else{
            bomb(minTower, maxTower);
        }
    }

    private static void bomb(Tower minTower, Tower maxTower) {
        int x = maxTower.x;
        int y = minTower.y;

        // power 수정
        maxTower.power -= minTower.power;
        map[x][y].power -= minTower.power;

        isAttacked = new boolean[max_N][max_M];
        isAttacked[x][y] = true;

        // 좌표 수정
        int lx = x - 1;
        int rx = x + 1;
        int ly = y - 1;
        int ry = y + 1;

        if (lx <= 0) {
            lx = N;
        }
        if (rx >= N + 1) {
            rx = 1;
        }

        if (ly <= 0) {
            ly = M;
        }
        if (ry >= M + 1) {
            ry = 1;
        }

        map[lx][ly].power = lowTower.power / 2;
        map[lx][y].power = lowTower.power / 2;
        map[lx][ry].power = lowTower.power / 2;
        map[x][ly].power = lowTower.power / 2;
        map[x][ry].power = lowTower.power / 2;
        map[rx][ly].power = lowTower.power / 2;
        map[rx][y].power = lowTower.power / 2;
        map[rx][ry].power = lowTower.power / 2;

        isAttacked[lx][ly] = true;
        isAttacked[lx][y] = true;
        isAttacked[lx][ry] = true;
        isAttacked[x][ly] = true;
        isAttacked[x][ry] = true;
        isAttacked[rx][ly] = true;
        isAttacked[rx][y] = true;
        isAttacked[rx][ry] = true;
    }

    private static void razer(Tower minTower, Tower maxTower) {
        int sx = minTower.x;
        int sy = minTower.y;

        int ex = maxTower.x;
        int ey = maxTower.y;

        // power 값 바꾸기
        maxTower.power -= minTower.power;
        map[maxTower.x][maxTower.y].power = maxTower.power;

        isAttacked = new boolean[max_N][max_M];
        isAttacked[maxTower.x][maxTower.y] = true;

        while (sx != ex && sy != ey) {
            backTower[ex][ey].power -= minTower.power/2;
            ex = backTower[ex][ey].x;
            ey = backTower[ex][ey].y;
            isAttacked[ex][ey] = true;
        }
    }

    static boolean[][] isAttacked;

    // bfs, 경로 저장
    static int[] dx = {0, 1, 0, -1}; // 우하좌상
    static int[] dy = {1, 0, -1, 0};
    static Tower[][] backTower;
    private static boolean hasRoute(Tower minTower, Tower maxTower) {
        Queue<Tower> que = new LinkedList<>();
        boolean[][] visited = new boolean[N + 1][M + 1];
        que.add(minTower);
        visited[minTower.x][minTower.y] = true;
        backTower = new Tower[N + 1][M + 1];

        while (!que.isEmpty()) {
            Tower poll = que.poll();

            if (poll.x == maxTower.x && poll.y == maxTower.y) {
                return true;
            }

            for (int d = 0; d < 4; d++) {
                int nx = poll.x + dx[d];
                int ny = poll.y + dy[d];
                
                // 범위 밖이면 반대편으로
                if (nx == N + 1) { // 아래쪽으로 진행중
                    nx = 1;
                }
                if (nx == 0) { // 위쪽으로 진행중
                    nx = N;
                }
                if (ny == M + 1) { // 오른쪽으로 진행중
                    ny = 1;
                }
                if (ny == 0) { // 왼쪽으로 진행중
                    ny = M;
                }
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

// 1. 공격자 선정
// 우선순위
// 1. 공격력이 가장 낮은 포탑
// 2. 가장 최근에 공격한 포탑
// 3. 행과 열의 합이 가장 큰 포탑
// 4. 열의 값이 가장 큰 포탑

// 2. 공격
// 우선순위
// 1. 공격력이 가장 높은 포탑
// 2. 공격한지 가장 오래된 포탑
// 3. 행과 열의 합이 가장 작은 포탑
// 4. 열의 값이 가장 작은 포탑

// 2-1. 레이저 공격
// 상하좌우 4방향
// 부서진 포탑은 지날수없음
// 막힌 방향으로 진행한다면, 반대편으로 나온다.
// 최단경로로 공격
// 최단경로가 2개 이상일 경우, 우 하 좌 상 순서로 선택
// 공격자에게는 공격력만큼 피해, 나머지 경로는 /2

// 2-2. 포탄 공격
// 8방 피해 : 공격자는 공격력만큼, 나머지는 /2
// 가장자리에 떨어지면, 반대편에도 영향 미침

// 3. 포탑 부서짐

// 4. 포탑 정비
// 공격력이 1씩올라감.

// 이후 2부터 반복

// 공격력이 가장 강한 포탑 출력.

// 유의
// 부서지지않은 포탑이 1개가되면 즉시 중지
// (1,1) 부터 시작