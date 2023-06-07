package com.naturalprogrammer.springmvc.common.features.get_context;

import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GetContextIntegrationTest extends AbstractIntegrationTest {

    @Test
    void should_GetContext() throws Exception {

        // when, then
        mvc.perform(get("/context"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(ContextResource.CONTENT_TYPE))
                .andExpect(jsonPath("keys", hasSize(1)))
                .andExpect(jsonPath("keys[0].id").value("e0498dad-4f5f-40cf-86e3-2726ec78463d"))
                .andExpect(jsonPath("keys[0].publicKey").value("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAltQ21+NEs7BvISW7h10TErdhBNFR5Dgdfff0tYDjCnWNOaYG/iTpdQkm9oV7ZrXnbQFLssc9cfiDjDrf+VTVDOAE1tunSaa1rM9bHqIf1JXTontwht2cABGUQgkp3+kWNY+1OSu6ESSCkUILracT85ccFeCtA20wGrHhMY2bODI4mJi4He7Mr4A5sR1eJQV4GJZJBLIH9H/6HDDl7azNBxosz7hQ5Ny2wSkonssdThhyK84MEoEpo5dY2/IHJjN6+X4B+CI7MyWLWWpGIa0R37zV5LDtbePmBcfFZfFjv2HDFEx2Vb5EkBajQ88XvRXcWKhE8wI89Hjsk00hmZzqNQIDAQAB"));
    }
}