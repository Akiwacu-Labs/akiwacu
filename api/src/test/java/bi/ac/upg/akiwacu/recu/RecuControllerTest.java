package bi.ac.upg.akiwacu.recu;

import bi.ac.upg.akiwacu.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecuController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecuService recuService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldDownloadPdfInline() throws Exception {
        byte[] pdf = "%PDF-test".getBytes();
        when(recuService.telecharger(7L)).thenReturn(pdf);

        mockMvc.perform(get("/api/recus/7/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"recu-7.pdf\""))
                .andExpect(content().bytes(pdf));
    }
}
