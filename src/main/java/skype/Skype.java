package skype;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.text.SimpleDateFormat;

public class Skype {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm.ss");
    private static long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SkypeManager());
            serverBootstrap.bind(60010).syncUninterruptibly().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void log(String data) {
        System.out.println("[" + dateFormat.format(System.currentTimeMillis() - startTime) + "] " + data);
    }

}
