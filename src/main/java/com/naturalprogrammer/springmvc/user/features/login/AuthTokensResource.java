package com.naturalprogrammer.springmvc.user.features.login;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record AuthTokensResource(

        @Schema(
                title = "Resource-token for generating tokens next time (using GET /user-id/resource-token)",
                example = "eyJraWQiOiJlMDQ5OGRhZC00ZjVmLTQwY2YtODZlMy0yNzI2ZWM3ODQ2M2QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI4NmI0ODY0MS00NjU2LTQ1YmEtYWY4NS1lZjE0OGM3Mjg0ZjUiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzY29wZSI6InJlc291cmNlX3Rva2VuIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiZXhwIjoxNjgwNDM1NjAwLCJpYXQiOjE2ODA0MDIxNzV9.agJFF6L3PjjY7r1lPfcPiBI8i-SrcfCvrJLqHu3_DnGklD-ayb2nzVUFM-9qXQ2n9X2u5qD_YBiMPQaYn2aemK-EuUr85YMdcmiipYQJxfjcJWXweBcVMyna6fGvZHP32Gz4ANYOJxu0vFXvYDpTIUuhTcW4pKhXJdjTF_JvHFEmIOzzmFTEW08JqoWi2Q702XzqKmz8Uk6F_zolxL-kRd4QsWfiB-a8C7y6k48ItqOvGJD20FQyd6bsRi7XqyXkvluvUPtzD87txKmJgaooVCdJschnnKcX-T-jsf6rFpv3NfYu5Uak0IIhyel7-LCUm9m81_kbSz2mUzID1em8wg"
        )
        String resourceToken,

        @Schema(
                title = "Access-token for accessing the API",
                example = "eyJraWQiOiJlMDQ5OGRhZC00ZjVmLTQwY2YtODZlMy0yNzI2ZWM3ODQ2M2QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI4NmI0ODY0MS00NjU2LTQ1YmEtYWY4NS1lZjE0OGM3Mjg0ZjUiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzY29wZSI6Im5vcm1hbCIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImV4cCI6MTY4MDQwMzk3NSwiaWF0IjoxNjgwNDAyMTc1fQ.O35Wbzg_FYa7gr_H5YZL1c2oUfzOzsD9TiTLWhxnhiZPSovfVudyKUpRIbRtVKgx7fokIhd6vwNeetPTIpGYfaVCT9ytGuieeLvC38axdL8007cPLamrhWS1q-0ZuKRuqNEwimxINqoAp7VAL7UJm_s-8fhcYy34T9BQXbW-bvATwcQMm6YMGUGN8yuBnYkzKHloKt4Q6w_7__m1hyFg1240R2bYcmMl5D4pHjnRDd0R3KHvSgI0kc7YaQcImZEz3YOjKpTIdAw1dqKX5OEpYfzUwl66I6N2gZtq3PY8hmJJBjvx-tBnHXcsjYQ9-9T7gFw7iev3wi37vvlpNmshBA", description = "To generate new tokens, use GET /{user-id}/resource-token (must be same user or an Admin)"
        )
        String accessToken,

        @Schema(
                title = "Till when the resource token is valid",
                example = "2029-12-29T12:13:56Z"
        )
        Instant resourceTokenValidUntil,

        @Schema(
                title = "Till when the access token is valid",
                example = "2028-11-28T11:44:51Z"
        )
        Instant accessTokenValidUntil
) {

    @Override
    public String toString() {
        return "AuthTokensResource{" +
                "resourceTokenValidUntil='" + resourceTokenValidUntil + '\'' +
                ", accessTokenValidUntil='" + accessTokenValidUntil + '\'' +
                '}';
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "auth-token.v1+json";
}
