package lv.rtu.autograderserver.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class Sandbox {

    public void test() {
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();

            DockerHttpClient.Request request = DockerHttpClient.Request.builder()
                    .method(DockerHttpClient.Request.Method.GET)
                    .path("/_ping")
                    .build();

            try (DockerHttpClient.Response response = httpClient.execute(request)) {
                System.out.println(response.getStatusCode());
                System.out.println(IOUtils.toString(response.getBody(), StandardCharsets.UTF_8));
            }

            DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
            dockerClient.pullImageCmd("ubuntu:latest").exec(new PullImageResultCallback()).awaitCompletion();


            // Run
            CreateContainerResponse container = dockerClient
                    .createContainerCmd("ubuntu:latest")
                    .withWorkingDir("/app")
                    // .withCmd("/bin/sh", "-c", "cat /app/project/test.txt")
                    .withCmd("/bin/sh", "-c", "ls -ahl project")
                    .exec();

            String content = "This is test content, hello world!";

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 TarArchiveOutputStream tar = new TarArchiveOutputStream(bos)) {
                TarArchiveEntry entry = new TarArchiveEntry("project/test.txt", true);
                entry.setSize(content.getBytes().length);
                entry.setMode(0700);
                tar.putArchiveEntry(entry);
                tar.write(content.getBytes());
                tar.closeArchiveEntry();
                tar.close();

                try (InputStream is = new ByteArrayInputStream(bos.toByteArray())) {
                    dockerClient.copyArchiveToContainerCmd(container.getId())
                            .withTarInputStream(is)
                            .withRemotePath("/app")
                            .withDirChildrenOnly(true)
                            .exec();
                }
            }


            StringBuilder sb = new StringBuilder();

            dockerClient.logContainerCmd(container.getId()).withStdOut(true).withStdErr(true).exec(new ResultCallback<Frame>() {
                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onNext(Frame object) {
                    System.out.println("<- ON NEXT ->");
                    System.out.println("<- " + new String(object.getPayload()) + " ->");
                    System.out.println("<----------->");

                    sb.append(new String(object.getPayload()));
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("<- ON ERROR ->");
                    System.out.println("<- " + throwable.getMessage() + " ->");
                    System.out.println("<----------->");
                }

                @Override
                public void onComplete() {
                    System.out.println("<- ON_COMPLETE ->");
                    System.out.println(sb.toString());
                    System.out.println("<--------------->");
                }

                @Override
                public void close() throws IOException {
                    System.out.println("<- CLOSE ->");
                }
            });

            System.out.println("CONTAINER_ID: " + container.getId());
            dockerClient.startContainerCmd(container.getId()).exec();

            Thread.sleep(10000);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Sandbox sb = new Sandbox();

        sb.test();
    }
}
