package com.iftm.client.resources;

//necessário para utilizar o MockMVC
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourceTest {

        @Autowired
        private MockMvc mockMVC;

        @MockBean
        private ClientService service;

        @Autowired
        private ObjectMapper objectMapper;

        /**
         * Caso de testes : Verificar se o endpoint get/clients/ retorna todos os
         * clientes existentes
         * Arrange:
         * - camada service simulada com mockito
         * - base de dado : 3 clientes
         * new Client(7l, "Jose Saramago", "10239254871", 5000.0,
         * Instant.parse("1996-12-23T07:00:00Z"), 0);
         * new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0,
         * Instant.parse("1996-12-23T07:00:00Z"), 0);
         * new Client(8l, "Toni Morrison", "10219344681", 10000.0,
         * Instant.parse("1940-02-23T07:00:00Z"), 0);
         * - Uma PageRequest default
         * 
         * @throws Exception
         */
        @Test
        @DisplayName("Verificar se o endpoint get/clients/ retorna todos os clientes existentes")
        public void testarEndPointListarTodosClientesRetornaCorreto() throws Exception {
                // arrange
                int quantidadeClientes = 3;
                // configurando o Mock ClientService
                List<ClientDTO> listaClientes;
                listaClientes = new ArrayList<ClientDTO>();
                listaClientes.add(new ClientDTO(new Client(7L, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(8L, "Toni Morrison", "10219344681", 10000.0,
                                Instant.parse("1940-02-23T07:00:00Z"), 0)));

                Page<ClientDTO> page = new PageImpl<>(listaClientes);

                Mockito.when(service.findAllPaged(Mockito.any())).thenReturn(page);
                // fim configuração mockito

                // act

                ResultActions resultados = mockMVC.perform(get("/clients/").accept(MediaType.APPLICATION_JSON));

                // assign
                resultados
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 7L).exists())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 4L).exists())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 8L).exists())
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Toni Morrison").exists())
                                .andExpect(jsonPath("$.totalElements").exists())
                                .andExpect(jsonPath("$.totalElements").value(quantidadeClientes))

                                // novos andExpect
                                // Milena
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Jose Saramago").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10239254871").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 5000.0).exists())

                                // Diego
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10219344681").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 10000.0).exists())
                                .andExpect(jsonPath("$.content[?(@.children == '%s')]", 0).exists())

                                // Barbara
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Carolina Maria de Jesus").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10419244771").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 7500.0).exists());
        }

        // Milena
        @Test
        @DisplayName("Verificar se o endpoint /{id} deleta id existente")
        public void testarEndPointDeleteIdValido() throws Exception {

                Mockito.doNothing().when(service).delete(Mockito.anyLong());

                ResultActions resultado = mockMVC.perform(delete("/clients/{id}", 1L));

                Mockito.verify(service, Mockito.times(1)).delete(1L);

                resultado
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Verificar se o endpoint /{id} retorna erro para delete de id inexistente")
        public void testarEndPointDeleteIdInvalido() throws Exception {

                Mockito.doThrow(new ResourceNotFoundException("Resource not found")).when(service)
                                .delete(Mockito.anyLong());

                ResultActions resultado = mockMVC.perform(delete("/clients/{id}", 1000L));

                Mockito.verify(service, Mockito.times(1)).delete(1000L);

                resultado
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Resource not found"));
        }

        @Test
        @DisplayName("Verificar se o endpoint /{id} atualiza um cliente com id válido")
        public void testarEndPointUpdadeIdValido() throws Exception {

                // arrange
                ClientDTO updateCliente1 = new ClientDTO(7L, "Jose Antonio Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 2);
                ClientDTO updateCliente2 = new ClientDTO(4L, "Carolina Maria de Jesus de Paula", "10419244771", 7500.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 1);

                Mockito.when(service.update(Mockito.anyLong(), Mockito.any(ClientDTO.class)))
                                .thenReturn(updateCliente1)
                                .thenReturn(updateCliente2);
                // fim configuração mockito

                // act
                String clienteJson1 = objectMapper.writeValueAsString(updateCliente1);
                String clienteJson2 = objectMapper.writeValueAsString(updateCliente2);

                ResultActions resultadoCliente1 = mockMVC.perform(put("/clients/7")
                                .content(clienteJson1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                ResultActions resultadoCliente2 = mockMVC.perform(put("/clients/4")
                                .content(clienteJson2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                // assign
                resultadoCliente1
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(7L))
                                .andExpect(jsonPath("$.name").value("Jose Antonio Saramago"))
                                .andExpect(jsonPath("$.cpf").value("10239254871"))
                                .andExpect(jsonPath("$.income").value(5000.0))
                                .andExpect(jsonPath("$.children").value(2));

                resultadoCliente2
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(4L))
                                .andExpect(jsonPath("$.name").value("Carolina Maria de Jesus de Paula"))
                                .andExpect(jsonPath("$.cpf").value("10419244771"))
                                .andExpect(jsonPath("$.income").value(7500.0))
                                .andExpect(jsonPath("$.children").value(1));
        }

        @Test
        @DisplayName("Verificar se o endpoint /{id} retorna erro quando atualiza um cliente com id inexistente")
        public void testarEndPointUpdateIdInvalido() throws Exception {

                // arrange
                ClientDTO updateCliente1 = new ClientDTO(7L, "Jose Antonio Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 2);

                Mockito.when(service.update(Mockito.anyLong(), Mockito.any(ClientDTO.class)))
                                .thenThrow(new ResourceNotFoundException("Resource not found"));
                // fim configuração mockito

                // act and assign
                String clienteJson1 = objectMapper.writeValueAsString(updateCliente1);

                // Fazendo a chamada PUT e verificando o retorno de erro 404
                ResultActions resultadoCliente1 = mockMVC.perform(put("/clients/{id}", 100L)
                                .content(clienteJson1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                resultadoCliente1
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Resource not found"));

        }

        // Diego
        @Test
        @DisplayName("Verificar se o endpoint /incomeGreaterThan/ retorna dado ")
        public void testarEndPointIncomeGreaterThanRetornaCorreto() throws Exception {

                // Arrange
                Double maior = 5000.0;

                // configurando o mock ClientService
                List<ClientDTO> listaClientes;
                listaClientes = new ArrayList<ClientDTO>();
                listaClientes.add(new ClientDTO(new Client(7L, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(8L, "Toni Morrison", "10219344681", 10000.0,
                                Instant.parse("1940-02-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(9L, "Sebastião Siveira", "17256987123", 5000.0,
                                Instant.parse("1952-08-31T15:30:00Z"), 1)));

                Page<ClientDTO> page = new PageImpl<>(listaClientes);

                Mockito.when(
                                service.findByIncomeGreaterThan(Mockito.any(), Mockito.anyDouble())).thenReturn(page);
                // fim configuração mockito

                // Act
                ResultActions resultado = mockMVC.perform(get("/clients/incomeGreaterThan/")
                                .param("income", String.valueOf(maior)).accept(MediaType.APPLICATION_JSON));

                // Assign
                resultado
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 4L).exists())
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Carolina Maria de Jesus").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10419244771").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 7500.0).exists())
                                .andExpect(jsonPath("$.content[?(@.children == '%s')]", 0).exists())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 8L).exists())
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Toni Morrison").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10219344681").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 10000.0).exists())
                                .andExpect(jsonPath("$.content[?(@.children == '%s')]", 0).exists());
        }

        @Test
        @DisplayName("Verificar se o endpoint /cpf/ retorna dado")
        public void testarEndPointfindByCPFRetornaCorreto() throws Exception {

                // Arrange
                String parteCPF = "92";

                // configurando o mock ClientService
                List<ClientDTO> listaClientes;
                listaClientes = new ArrayList<ClientDTO>();
                listaClientes.add(new ClientDTO(new Client(7L, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(8L, "Toni Morrison", "10219344681", 10000.0,
                                Instant.parse("1940-02-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(9L, "Sebastião Siveira", "17256987123", 5000.0,
                                Instant.parse("1952-08-31T15:30:00Z"), 1)));

                Page<ClientDTO> page = new PageImpl<>(listaClientes);

                Mockito.when(
                                service.findByCpfLike(Mockito.any(), Mockito.anyString())).thenReturn(page);
                // fim configuração mockito

                // Act
                ResultActions resultado = mockMVC.perform(get("/clients/cpf/").param("cpf", String.valueOf(parteCPF))
                                .accept(MediaType.APPLICATION_JSON));

                // Assign
                resultado
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 7L).exists())
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Jose Saramago").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10239254871").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 5000.0).exists())
                                .andExpect(jsonPath("$.content[?(@.children == '%s')]", 0).exists())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 4L).exists())
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Carolina Maria de Jesus").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10419244771").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 7500.0).exists())
                                .andExpect(jsonPath("$.content[?(@.children == '%s')]", 0).exists());
        }

        @Test
        @DisplayName("Verificar se o endpoint /clients/ realiza o insert corretamente")
        public void testarEndPointInsertRetornaCorreto() throws Exception {

                // arrange
                ClientDTO newCliente1 = new ClientDTO(7L, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0);
                ClientDTO newCliente2 = new ClientDTO(4L, "Carolina Maria de Jesus", "10419244771", 7500.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 1);

                ClientDTO savedCliente1 = new ClientDTO(7L, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0);
                ClientDTO savedCliente2 = new ClientDTO(4L, "Carolina Maria de Jesus", "10419244771", 7500.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 1);

                Mockito.when(service.insert(Mockito.any(ClientDTO.class))).thenReturn(savedCliente1)
                                .thenReturn(savedCliente2);
                // fim configuração mockito

                // act

                String clienteJson1 = objectMapper.writeValueAsString(newCliente1);
                String clienteJson2 = objectMapper.writeValueAsString(newCliente2);

                ResultActions resultadoCliente1 = mockMVC.perform(post("/clients/")
                                .content(clienteJson1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                ResultActions resultadoCliente2 = mockMVC.perform(post("/clients/")
                                .content(clienteJson2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON));

                // assign
                resultadoCliente1
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(7L))
                                .andExpect(jsonPath("$.name").value("Jose Saramago"))
                                .andExpect(jsonPath("$.cpf").value("10239254871"))
                                .andExpect(jsonPath("$.income").value(5000.0))
                                .andExpect(jsonPath("$.children").value(0));

                resultadoCliente2
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(4L))
                                .andExpect(jsonPath("$.name").value("Carolina Maria de Jesus"))
                                .andExpect(jsonPath("$.cpf").value("10419244771"))
                                .andExpect(jsonPath("$.income").value(7500.0))
                                .andExpect(jsonPath("$.children").value(1));

        }

        // Barbara
        @Test
        @DisplayName("Verificar se o endpoint /id/{id} retorna dado em caso de id existente")
        public void testarEndPointfindByIdRetornaCorreto() throws Exception {

                // Arrange
                int idCliente = 1;

                // configurando o mock ClientService
                ClientDTO cliente = new ClientDTO(new Client(1L, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0));

                Mockito.when(
                                service.findById(1L)).thenReturn(cliente);
                // fim configuração mockito

                // Act
                ResultActions resultado = mockMVC.perform(get("/clients/id/1").accept(MediaType.APPLICATION_JSON));

                // Assign
                resultado
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.id", 1L).exists())
                                .andExpect(jsonPath("$.id").value(idCliente))
                                .andExpect(jsonPath("$.name", "Jose Saramago").exists())
                                .andExpect(jsonPath("$.cpf", "10239254871").exists())
                                .andExpect(jsonPath("$.income", 5000.0).exists())
                                .andExpect(jsonPath("$.children", 0).exists());
        }

        @Test
        @DisplayName("Verificar se o endpoint /id/{id} retorna exception em caso de id inexistente")
        public void testarEndPointfindByIdComIdInexistente() throws Exception {

                // Arrange
                // configurando o mock ClientService
                Mockito.when(
                                service.findById(100L)).thenThrow(new ResourceNotFoundException("Resource not found"));
                // fim configuração mockito

                // Act
                ResultActions resultado = mockMVC.perform(get("/clients/id/100").accept(MediaType.APPLICATION_JSON));

                // Assign
                resultado
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Resource not found"));

        }

        @Test
        @DisplayName("Verificar se o endpoint /income/ retorna dado")
        public void testarEndPointfindByIncomeRetornaCorreto() throws Exception {

                // Arrange
                Double salarioResultado = 5000.0;

                // configurando o mock ClientService
                List<ClientDTO> listaClientes;
                listaClientes = new ArrayList<ClientDTO>();
                listaClientes.add(new ClientDTO(new Client(7L, "Jose Saramago", "10239254871", 5000.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0,
                                Instant.parse("1996-12-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(8L, "Toni Morrison", "10219344681", 10000.0,
                                Instant.parse("1940-02-23T07:00:00Z"), 0)));
                listaClientes.add(new ClientDTO(new Client(9L, "Sebastião Siveira", "17256987123", 5000.0,
                                Instant.parse("1952-08-31T15:30:00Z"), 1)));

                Page<ClientDTO> page = new PageImpl<>(listaClientes);

                Mockito.when(
                                service.findByIncome(Mockito.any(), Mockito.anyDouble())).thenReturn(page);
                // fim configuração mockito

                // Act
                ResultActions resultado = mockMVC.perform(get("/clients/income/")
                                .param("income", String.valueOf(salarioResultado)).accept(MediaType.APPLICATION_JSON));

                // Assign
                resultado
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 7L).exists())
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Jose Saramago").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "10239254871").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 5000.0).exists())
                                .andExpect(jsonPath("$.content[?(@.children == '%s')]", 0).exists())
                                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 9L).exists())
                                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Sebastião Siveira").exists())
                                .andExpect(jsonPath("$.content[?(@.cpf == '%s')]", "17256987123").exists())
                                .andExpect(jsonPath("$.content[?(@.income == '%s')]", 5000.0).exists())
                                .andExpect(jsonPath("$.content[?(@.children == '%s')]", 1).exists());
        }

}
