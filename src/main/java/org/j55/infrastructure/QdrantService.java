package org.j55.infrastructure;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QdrantService {

    private final QdrantClient qdrantClient;

    public QdrantService() {
        qdrantClient = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build());
    }

    @SneakyThrows
    public void createCollectionIfNotExists(String collectionName, int dimensions) {
        if (collectionExists(collectionName)) {
            log.info("Collection {} already exists. Skipping.", collectionName);
            return;
        }
        log.info("creating collection {}", collectionName);
        qdrantClient.createCollectionAsync(collectionName,
                Collections.VectorParams.newBuilder()
                    .setDistance(Collections.Distance.Cosine)
                    .setSize(dimensions)
                    .setOnDisk(true)
                    .build())
            .get();
        log.info("Collection {} created", collectionName);

    }

    @SneakyThrows
    public boolean collectionExists(String collectionName) {
        return qdrantClient.listCollectionsAsync().get().contains(collectionName);

    }

    public ListenableFuture<Points.UpdateResult> upsert(String collectionName, Points.PointStruct pointStruct) {
        return qdrantClient.upsertAsync(collectionName, List.of(pointStruct));
    }

    public ListenableFuture<Points.UpdateResult> upsert(String collectionName, List<Points.PointStruct> pointStructList) {
        return qdrantClient.upsertAsync(collectionName, pointStructList);
    }

    @SneakyThrows
    public List<Points.ScoredPoint> search(String collectionName, List<Float> vector) {
        var searchCriteria = Points.SearchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllVector(vector)
            .setLimit(5)
            .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
            .build();
        return qdrantClient.searchAsync(searchCriteria).get();
    }
}
