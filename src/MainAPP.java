import javax.swing.*;
import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;

public class MainAPP extends JFrame {

    Scanner scanner = new Scanner(System.in);
    FileTransferClient client;
    FileTransferServer server;
    FileDESHelper fileDESHelper = new FileDESHelper("123456");

    public static void main(String[] args) {
        MainAPP app = new MainAPP();
        app.ShowMenuView();
    }

    private void ShowMenuView() {
        System.out.println("===============欢迎使用星光文件加密传输工具======================");
        System.out.print("请选择本计算机的角色：[1]接收方，[2]发送方,[q]退出:");
        String cmd = scanner.next();
        while (true) {
            if(cmd.equals("1")){
                ActAsServer();
                System.out.println("\n===============欢迎使用星光文件加密传输工具======================");
                System.out.print("请选择本计算机的角色：[1]接收方，[2]发送方,[q]退出:");
                cmd = scanner.next();
            }else if(cmd.equals("2")){
                ActAsSender();
            }else if(cmd.equals("q")){
                System.out.println("感谢您的使用！");
                break;
            } else {
                System.out.println("您的输入非法，请重写输入.");
                cmd = scanner.next();
            }
        }
    }

    private void ActAsSender() {
        String address;
        int port;
        System.out.print("请输入目标计算机的网络IP：");
        while (true){
            address = scanner.next();
            if (address.isEmpty()) {
                System.out.print("IP地址不能为空，请重新输入：");
                continue;
            }else{
                break;
            }
        }
        System.out.print("请输入目标计算机接收服务所在的端口号：");
        while (true) {
            if (!scanner.hasNextInt()) {
                scanner.nextLine();
                System.out.println("端口号非法，请输入新的端口号");
                continue;
            } else {
                port = scanner.nextInt();
            }
            break;
        }
        System.out.println("正在连接到计算机[" + address + "]" + ":" + port);
        try {
            client = new FileTransferClient(address, port);
            System.out.println("成功连接到目标计算机!");
            selectFileToSend();
        } catch (Exception e) {
            System.out.println("服务不存在，请检查文件接收端是否启用文件接收服务。");
            e.printStackTrace();
        }
    }

    private void selectFileToSend() {
        System.out.println("请在弹窗中选择您想要传送的文件：");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("请选择您想要传送的文件");
        fileChooser.showDialog(this, "确认");
        File file = fileChooser.getSelectedFile();
        System.out.println("您选择的文件为：[" + file.getAbsolutePath() + "]");
        System.out.println("是否对将要发送的文件进行加密处理？(y/n):");
        String cmd;
        while (true) {
            cmd = scanner.next();
            if (cmd.equals("y")) {
                String encryptFilePath = encryptFile(file);
                if(encryptFilePath == null){
                    return;
                }else {
                    file = new File(encryptFilePath);
                    JFileChooser fileShow = new JFileChooser(file.getAbsolutePath());
                    fileShow.setDialogType(JFileChooser.OPEN_DIALOG);
                    fileShow.setDialogTitle("查看已加密文件");
                    fileShow.showDialog(this,"我知道了");
                    break;
                }
            } else if (cmd.equals("n")) {
                break;
            } else {
                System.out.print("您的输入为非法输入，请重新输入(y/n):");
                continue;
            }
        }
        try {
            client.sendFile(file);
        } catch (Exception e) {
            System.out.println("文件错误或不存在，请检查您的操作");
        }

    }

    private String encryptFile(File file){
        String srcFilePath = file.getAbsolutePath();
        String destFilePath = file.getAbsolutePath() + ".des";
        try {
            fileDESHelper.encrypt(srcFilePath,destFilePath);
            System.out.println("文件加密成功，文件加密路径为："+destFilePath);
            return destFilePath;
        }catch (Exception e){
            System.out.println("文件加密失败，请重写发送。");
            return null;
        }
    }

    private void ActAsServer() {
        System.out.print("请输入文件接收服务所在端口号：");
        int port;
        while (true) {
            if (!scanner.hasNextInt()) {
                scanner.nextLine();
                System.out.println("您的端口号非法，请输入新的端口号");
                continue;
            } else {
                port = scanner.nextInt();
            }
            break;
        }
        ServerInit(port);
    }

    private void ServerInit(int port) {
        String localIP = "";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            localIP = inetAddress.getHostAddress();
        } catch (Exception e) {
            localIP = "未知";
        }
        try {
            server = new FileTransferServer(port);
            System.out.println("文件接收服务已经启动\n服务所在IP："
                    + localIP + "  \n服务端口：" + server.getLocalPort());
            System.out.println("监听文件传输中....");
            server.load();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("文件接收服务启动失败，端口号" + port + "被占用！");
        }
    }
}
