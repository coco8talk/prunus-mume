package com.coco8talk.pm.api.user.client;

import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.api.user.service.UserApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RemoteUserApi implements UserApi {

    private final RestClient restClient;

    public RemoteUserApi(RestClient.Builder builder,
                         @Value("${pm.remote.user-base-url:http://localhost:8080/api}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl + "/internal/users").build();
    }

    @Override
    public List<UserView> getByIds(Collection<Long> userIds) {
        return restClient.post()
                .uri("/batch")
                .body(userIds)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public UserView getById(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return restClient.get()
                    .uri("/{userId}", userId)
                    .retrieve()
                    .body(UserView.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        }
    }

    @Override
    public void grantVip(Long userId, Period period) {
        restClient.post()
                .uri("/{userId}/vip", userId)
                .body(Map.of("period", period.toString()))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public SessionUserDTO toSessionDto(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return restClient.get()
                    .uri("/{userId}/session", userId)
                    .retrieve()
                    .body(SessionUserDTO.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        }
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        restClient.put()
                .uri("/{userId}/avatar", userId)
                .body(Map.of("avatarUrl", avatarUrl))
                .retrieve()
                .toBodilessEntity();
    }
}
