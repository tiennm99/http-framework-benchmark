package ph.benchmark.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;

import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.LinkedList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main
{
	static final int PORT = 8080;

	//private static final LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);

	public static void main(String[] args)
	{
		// Configure the server.
		ServerBootstrap b = new ServerBootstrap();
		b.option(ChannelOption.SO_BACKLOG, 1024);
		//b.handler(loggingHandler);

		EventLoopGroup bossGroup;
		EventLoopGroup workerGroup;
		final int worker_count = 4;

		if (Epoll.isAvailable())
		{
			Epoll.ensureAvailability();

			bossGroup = new EpollEventLoopGroup(1);
			workerGroup = new EpollEventLoopGroup(worker_count);

			b.channelFactory(new ChannelFactory<ServerChannel>()
			{
				@Override
				public ServerChannel newChannel()
				{
					return new EpollServerSocketChannel();
				}
			});

			System.out.println("Use epoll.");
		}
		else if (KQueue.isAvailable())
		{
			KQueue.ensureAvailability();

			bossGroup = new KQueueEventLoopGroup(1);
			workerGroup = new KQueueEventLoopGroup(worker_count);

			b.channelFactory(new ChannelFactory<ServerChannel>()
			{
				@Override
				public ServerChannel newChannel()
				{
					return new KQueueServerSocketChannel();
				}
			});

			System.out.println("Use kqueue.");
		}
		else
		{
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup(worker_count);

			b.channelFactory(new ChannelFactory<ServerChannel>()
			{
				@Override
				public ServerChannel newChannel()
				{
					return new NioServerSocketChannel();
				}
			});

			System.out.println("Use nio.");
		}

		try
		{
			b.group(bossGroup, workerGroup)
					.childHandler(new SimpleHttpServerInitializer());

			Channel ch = b.bind(PORT).sync().channel();

			System.out.println("Netty http server is ready at port: " + PORT);

			ch.closeFuture().sync();
		}
		catch (Exception ex)
		{
			System.err.printf("Starting netty http server got exception: %s \n", ex);
		}
		finally
		{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}