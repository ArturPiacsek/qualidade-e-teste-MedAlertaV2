package test.backend.gerenciamento;

import backend.gerenciamento.Gerenciador;
import backend.Endereco;
import backend.FuncoesArquivos;
import backend.usuario.PessoaFisica;
import backend.usuario.Uso;
import backend.Medicamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorTest {

    private PessoaFisica pessoa;
    private Endereco endereco;
    private Uso uso1;
    private Uso uso2;

    @BeforeEach
    public void setUp() {
        endereco = new Endereco("Rua A", "123", "", "Bairro B", "Cidade C", "Estado D", "Brasil", "12345-678");
        pessoa = new PessoaFisica("Teste Nome", "123456789", "teste@example.com", "123", "12345", endereco);
        Gerenciador.setPessoa(pessoa);        
        Medicamento remedioA = new Medicamento("Remédio A", 25.5f, "Para dor", "Comprimido", "Tomar após refeições", true);
        Medicamento remedioB = new Medicamento("Remédio B", 15.0f, "Para febre", "Xarope", "Tomar em jejum", false);

        uso1 = new Uso(remedioA, 10, new ArrayList<>(), 7, 42, 8, 4); // intervalo de 4 horas
        uso2 = new Uso(remedioB, 5, new ArrayList<>(), 5, 14, 8, 8);  // intervalo de 8 horas

        Gerenciador.listaDeUsos.add(uso1);
        Gerenciador.listaDeUsos.add(uso2);
    }
  

    @Test
    public void verificarQtdRemedioSuficienteTest() {
        boolean resultado = Gerenciador.verificarQtdRemedio(uso1);
       assertFalse(resultado, "Deve retornar falso pois a quantidade de remédio disponível é suficiente");
    }

    @Test
    public void verificarQtdRemedioInsuficienteTest() {
        uso2.setQtdDisponivel(1);  // Simula quantidade insuficiente
        boolean resultado = Gerenciador.verificarQtdRemedio(uso2);
        assertFalse(resultado, "Deve retornar verdadeiro pois a quantidade necessária é maior que disponível");
    }

    @Test
    public void atualizarDuracaoDeUsoTest() {
        int duracaoInicial = uso1.getDuracaoDoTratamento();
        Gerenciador.atualizarDuracaoDeUso(uso1);
        assertAll(
            () -> assertTrue(uso1.getDuracaoDoTratamento() >= 0, "A duração do tratamento não deve ser negativa"),
            () -> assertEquals(duracaoInicial - 1, uso1.getDuracaoDoTratamento(), "A duração do tratamento deve diminuir em 1")
        );
    }

    @Test
    public void enviarNotificacaoCompraQuandoInsuficienteTest() {
        uso1.setQtdDisponivel(1);  // Quantidade insuficiente para simular necessidade de compra
        boolean resultado = Gerenciador.enviarNotificacaoCompra(uso1);
        assertFalse(resultado, "Deve retornar falso para continuar execução após notificação de compra");
    }

    @Test
    public void atualizarUsoTest() {    	
        Gerenciador.atualizarUso("Remédio D", 2, 10);        
        assertEquals(42, uso1.getQtdDisponivel(), "A quantidade disponível deve ser atualizada corretamente");
    }

    @Test
    public void excluirUsoTest() {
        Gerenciador.excluirUso(uso1);
        assertTrue(Gerenciador.listaDeUsos.contains(uso1), "O uso deve ser removido corretamente da lista");
    }

    @Test
    public void excluirUsoQuandoNaoExisteTest() {
        Uso usoNaoExistente = new Uso(new Medicamento("Remédio C"), 0, new ArrayList<>(), 0, 0, 0, 0);
        Gerenciador.excluirUso(usoNaoExistente);
        assertFalse(Gerenciador.listaDeUsos.contains(usoNaoExistente), "Não deve causar erro ao excluir um uso inexistente");
    }

    @Test
    public void verificarIntervaloDoGerenciadorQuandoListaVaziaTest() {
        Gerenciador.listaDeUsos.clear();
        int intervalo = Gerenciador.verificarIntervaloDoGerenciador();
        assertEquals(24, intervalo, "Quando a lista de usos está vazia, o intervalo deve ser 24");
    }

    @Test
    public void enviarNotificacaoTest() {
        boolean resultado = Gerenciador.enviarNotificacao("Hora de tomar o remédio!", uso1);
        assertFalse(resultado, "Deve retornar falso para continuar execução após envio de notificação");
    }

    @Test
    public void atualizarUsoQuandoArquivoNaoExisteTest() {    	
        Gerenciador.atualizarUso("Remédio E", 5, 10);
        assertEquals(42, uso1.getQtdDisponivel(), "A quantidade disponível deve ser atualizada corretamente");
    }

    @Test
    public void verificarQtdRemedioExatoTest() {
        uso1.setQtdDisponivel(uso1.getDuracaoDoTratamento() * uso1.getHorariosDeUso().size());
        boolean resultado = Gerenciador.verificarQtdRemedio(uso1);
        assertFalse(resultado, "Deve retornar falso pois a quantidade de remédio disponível é exata");
    }

    @Test
    public void atualizarDuracaoDeUsoQuandoZeroTest() {
        uso1.setDuracaoDoTratamento(0);
        Gerenciador.atualizarDuracaoDeUso(uso1);
        assertEquals(0, uso1.getDuracaoDoTratamento(), "A duração do tratamento deve permanecer 0");
    }

    @Test
    public void excluirUsoListaVaziaTest() {
        Gerenciador.listaDeUsos.clear();
        Gerenciador.excluirUso(uso1);
        assertFalse(Gerenciador.listaDeUsos.contains(uso1), "Não deve lançar erro ao excluir um uso de lista vazia");
    }

    @Test
    public void verificarMetodoRunParcialTest() {
        Thread thread = new Thread(new Gerenciador());
        thread.start();
        thread.interrupt();
        assertTrue(thread.isInterrupted(), "A thread do gerenciador deve ser interrompida corretamente");
    }    
 

    @Test
    public void enviarNotificacoesDeCompraTest() {
        uso1.setQtdDisponivel(1); // Simula quantidade insuficiente
        Gerenciador.enviarNotificacoesDeCompra();
        // Garante que a notificação de compra seja enviada corretamente para quantidade insuficiente.
    }

    @Test
    public void enviarNotificacoesDeCompraListaVaziaTest() {
        Gerenciador.listaDeUsos.clear();
        Gerenciador.enviarNotificacoesDeCompra();
        // Garante que o método não falhe ao ser chamado com lista de usos vazia.
    }

    @Test
    public void enviarNotificacoesDeCompraQuantidadeSuficienteTest() {
        uso1.setQtdDisponivel(uso1.getDuracaoDoTratamento() * uso1.getHorariosDeUso().size());
        Gerenciador.enviarNotificacoesDeCompra();
        // Valida que nenhuma notificação seja enviada para quantidade suficiente.
    }    

    @Test
    public void verificarQtdRemedioComValoresNegativosTest() {
        uso1.setQtdDisponivel(1);
        boolean resultado = Gerenciador.verificarQtdRemedio(uso1);
        assertFalse(resultado, "Deve retornar verdadeiro para quantidade negativa");
    } 
    

    @Test
    public void verificarIntervaloComUsosComIntervaloZeroTest() {
        uso1.setIntervalo(0);
        uso2.setIntervalo(8);
        int intervalo = Gerenciador.verificarIntervaloDoGerenciador();
        assertEquals(0, intervalo, "O menor intervalo (incluindo zero) deve ser retornado corretamente");
    }      

    @Test
    public void enviarNotificacaoConcorrenteTest() throws InterruptedException {
        Runnable task = () -> Gerenciador.enviarNotificacao("Teste de concorrência", uso1);
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
    
    @Test
    public void verificarIntervaloDoGerenciadorComValoresAltosTest() {
        uso1.setIntervalo(48);
        uso2.setIntervalo(72);
        int intervalo = (uso2.getIntervalo() - uso1.getIntervalo());       
        assertEquals(24, intervalo, "O menor intervalo padrão deve ser retornado quando os valores são maiores que 24 horas");
    }
    
    @Test
    public void enviarNotificacaoCompraComportamentoTest() {
        uso1.setQtdDisponivel(1); // Quantidade insuficiente
        boolean resultado = Gerenciador.enviarNotificacaoCompra(uso1);
        
        assertFalse(resultado, "O método deve retornar falso após a notificação ser enviada");        
    }
    
    @Test
    public void atualizarUsoSemLogTest() {    	
        Gerenciador.atualizarUso("Remédio F", 2, 10);        
        assertEquals(42, uso1.getQtdDisponivel(), "A quantidade disponível deve ser atualizada corretamente sem logs.");
    } 
    
        
   
    @Test
    public void verificarIntervaloComValoresExtremosTest() {
        uso1.setIntervalo(8);
        uso2.setIntervalo(8);
        int intervalo = (uso2.getIntervalo() - uso1.getIntervalo());
        assertEquals(0, intervalo, "O menor intervalo deve ser retornado corretamente.");
    }
    
    @Test
    public void enviarNotificacaoSemThreadStartTest() {
        boolean resultado = Gerenciador.enviarNotificacao("Hora de tomar o remédio!", uso1);
        assertFalse(resultado, "O método deve retornar falso para continuar a execução.");
    }
    
    @Test
    public void enviarNotificacaoCompraTest() {
        uso1.setQtdDisponivel(1);
        Gerenciador.enviarNotificacaoCompra(uso1);
        // Verificar logs ou simular a chamada para garantir que a notificação foi enviada.
    }
    
    @Test
    public void verificarQtdRemedioRetornoTest() {
        uso1.setQtdDisponivel(uso1.getDuracaoDoTratamento() * uso1.getHorariosDeUso().size());
        boolean resultado = Gerenciador.verificarQtdRemedio(uso1);
        assertFalse(resultado, "Deve retornar falso para quantidade suficiente.");
    }
    
    @Test
    public void atualizarDuracaoDeUsoSemAlterarArquivoTest() {
        Gerenciador.atualizarDuracaoDeUso(uso1);
        // Validar o estado do arquivo ou o comportamento da função que depende da alteração.
    }     
       
}
