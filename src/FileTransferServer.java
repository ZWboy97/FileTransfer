
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * 文件传输Server端
 */
public class FileTransferServer extends ServerSocket {

    public static final int SERVER_PORT = 8899;

    private static DecimalFormat df = null;

    FileDESHelper fileDESHelper = new FileDESHelper("123456");

    static {
        // 设置数字格式，保留一位有效小数
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }

    public FileTransferServer() throws Exception {
        super(SERVER_PORT);
    }

    public FileTransferServer(int serverPort) throws Exception {
        super(serverPort);
    }

    /**
     * 线程处理每个客户端传输的文件,服务端支持多线程接收
     */
    public void load() throws Exception {
        while (true) {
            Socket socket = this.accept();
            // 每接收到一个Socket就建立一个新的线程来处理它
            new Thread(new Task(socket)).start();
        }
    }

    /**
     * 处理客户端传输过来的文件线程类
     */
    class Task implements Runnable {

        private Socket socket;

        private DataInputStream dis;

        private FileOutputStream fos;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                dis = new DataInputStream(socket.getInputStream());

                // 文件名和长度
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();
                File directory = new File("c:\\FileTransferDir");
                if (!directory.exists()) {
                    directory.mkdir();
                }
                File receiveFile = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                fos = new FileOutputStream(receiveFile);

                // 开始接收文件
                byte[] bytes = new byte[1024];
                int length = 0;
                while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                }
                System.out.println("======== 文件接收成功 [File Name：" + fileName + "] [Size：" + getFormatFileSize(fileLength) + "] ========");
                System.out.println("接收文件路径为：" + receiveFile.getAbsolutePath());

                if (receiveFile.getAbsolutePath().endsWith(".des")) {
                    System.out.println("该文件是加密文件，是否对其进行解密(y/n):");
                    Scanner scanner = new Scanner(System.in);
                    String cmd = scanner.next();
                    while (true) {
                        if (cmd.equals("y")) {
                            decryptFile(receiveFile);
                            break;
                        } else if (cmd.equals("n")) {
                            break;
                        } else {
                            System.out.println("您的输入有误，请重新输入(y/n)：");
                            continue;
                        }
                    }
                }
                System.out.println("\n\n文件接收服务继续运行中\nIP："
                        + getLocalSocketAddress() + "  \n服务端口：" + getLocalPort());
                System.out.println("监听文件传输中....");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                    if (dis != null)
                        dis.close();
                    socket.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private boolean decryptFile(File file) {
        if (!file.exists()) {
            return false;
        }
        String srcFilePath = file.getAbsolutePath();
        String destFilePath = file.getAbsolutePath().subSequence(0, srcFilePath.length() - ".des".length()).toString();
        try {
            fileDESHelper.decrypt(file.getAbsolutePath(), destFilePath);
            System.out.println("对收到的文件进行解密操作成功！\n解密文件路径为：" + destFilePath);
            return true;
        } catch (Exception e) {
            System.out.println("对接收到的文件进行解密操作失败！");
            return false;
        }

    }

    /**
     * 格式化文件大小
     */
    private String getFormatFileSize(long length) {
        double size = ((double) length) / (1 << 30);
        if (size >= 1) {
            return df.format(size) + "GB";
        }
        size = ((double) length) / (1 << 20);
        if (size >= 1) {
            return df.format(size) + "MB";
        }
        size = ((double) length) / (1 << 10);
        if (size >= 1) {
            return df.format(size) + "KB";
        }
        return length + "B";
    }
}
