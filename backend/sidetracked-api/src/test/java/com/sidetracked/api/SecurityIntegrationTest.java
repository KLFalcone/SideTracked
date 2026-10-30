package com.sidetracked.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
 * The real web layer against the sidetracked_test database.
 * Each test's data is rolled back afterwards.
 *
 * Needs MySQL running and DB_PASSWORD set, like the app itself.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void apiRequiresSigningIn() throws Exception {

        mvc.perform(get("/api/quests"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/player"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changesWithoutACsrfTokenAreRefused() throws Exception {

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration("kat@example.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void signingUpSignsYouIn() throws Exception {

        MockHttpSession session = signUp("kat@example.com");

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("kat@example.com"))
                .andExpect(jsonPath("$.hasPassword").value(true));

        mvc.perform(get("/api/player").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xp").value(0));
    }

    @Test
    void aWrongPasswordIsRefused() throws Exception {

        signUp("kat@example.com");

        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"kat@example.com","password":"wrong password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void oneUserCanNeverSeeOrChangeAnothersQuests() throws Exception {

        MockHttpSession kat = signUp("kat@example.com");
        MockHttpSession stranger = signUp("stranger@example.com");

        String created = mvc.perform(post("/api/quests").with(csrf())
                        .session(kat)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"kiss gbert's little head","type":"CARETAKER"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String questId = created.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/quests").session(stranger))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(put("/api/quests/" + questId + "/complete").with(csrf())
                        .session(stranger))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/quests/" + questId).with(csrf())
                        .session(stranger))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/quests").session(kat))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("kiss gbert's little head"));
    }

    @Test
    void signingOutEndsTheSession() throws Exception {

        MockHttpSession session = signUp("kat@example.com");

        mvc.perform(post("/api/auth/logout").with(csrf()).session(session))
                .andExpect(status().isOk());

        mvc.perform(get("/api/quests").session(session))
                .andExpect(status().isUnauthorized());
    }


    private MockHttpSession signUp(String email) throws Exception {

        return (MockHttpSession) mvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    }

    private String registration(String email) {

        return """
                {"email":"%s","password":"correct horse","displayName":"Tester"}
                """.formatted(email);
    }
}
