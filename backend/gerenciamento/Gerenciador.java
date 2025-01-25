package backend.gerenciamento;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import backend.FuncoesArquivos;
import backend.usuario.PessoaFisica;
import backend.usuario.Uso;

public class Gerenciador implements Runnable {
    public static ArrayList<Uso> listaDeUsos = new ArrayList<Uso>();
    public static PessoaFisica pessoa;

    public static void setPessoa(PessoaFisica p) {
        Gerenciador.pessoa = p;
    }

    public static void atualizarUso(String nomeMedicamento, int dose, int qtd) {
        int novaQtdDisponivel = 0;
        System.out.println(pessoa.getUsoListaUsoMedicamentos(nomeMedicamento));
        novaQtdDisponivel = qtd - dose;
        System.out.println(pessoa);
        File arquivoEstoque = new File(pessoa.getNomeArquivoUsos());
        if(arquivoEstoque.exists()){
            List<Uso> estoque = PessoaFisica.resgatarListaUsoMedicamentosArquivo(pessoa.getNomeArquivoUsos());
            pessoa.setListaUsoMedicamentos(estoque, false);
        }
        pessoa.atualizarQntRemediosListaUsoMedicamentos(nomeMedicamento, novaQtdDisponivel);
    }

    public static int verificarIntervaloDoGerenciador() {
        int menor = 24;
        if (listaDeUsos != null) {
            for (Uso uso : listaDeUsos) {
                if (uso.getIntervalo() < menor) {
                    menor = uso.getIntervalo();
                }
            }
        }
        return menor;
    }

    public static boolean enviarNotificacao(String notificacao, Uso uso) {
        Runnable runNotify = () -> {
            boolean tomouRemedio = Notificacao.notificar(notificacao);
            if (tomouRemedio) {
                System.out.println(uso.getRemedio().getNome());
                System.out.println(uso.getDose());
                atualizarUso(uso.getRemedio().getNome(), uso.getDose(), uso.getQtdDisponivel());
            }
        };
        Thread threadNotify = new Thread(runNotify);
        threadNotify.start();

        return false; //para que o programa continue
    }

    public static boolean enviarNotificacaoCompra(Uso uso) {
        String notificacao = "Existem apenas " + uso.getQtdDisponivel() + " comprimidos do seu remédio "
                + uso.getRemedio().getNome() + "\n" + "É nessário comprar mais para terminar seu tratamento!";

        Runnable runNotify = () -> {
            Notificacao.notificarCompra(notificacao);
        };
        Thread threadNotify = new Thread(runNotify);
        threadNotify.start();

        return false; //para que o programa continue
    }

    public static boolean verificarQtdRemedio(Uso uso) {
        boolean remedioAcabando = false;
        int qtd = uso.getQtdDisponivel();
        int duracao = uso.getDuracaoDoTratamento();
        int qtdAoDia = uso.getHorariosDeUso().size();
        int qtdNecessaria = duracao * qtdAoDia;

        if (qtdNecessaria > qtd) {
            remedioAcabando = true;
        }
        return remedioAcabando;
    }

    public static void atualizarDuracaoDeUso(Uso uso) {
        if(uso.getDuracaoDoTratamento()==0) return;
        uso.setDuracaoDoTratamento(uso.getDuracaoDoTratamento() - 1);
        String strUso = uso.toString();
        FuncoesArquivos.alterarLinhaArquivo(pessoa.getNomeArquivoUsos(), uso.getRemedio().getNome(), strUso);
    }

    public static void excluirUso(Uso uso) {
        Gerenciador.pessoa.removerUsoNaListaUsoMedicamentos(uso.getRemedio().getNome());
    }

    @Override
    public void run() {
        while (true) {
            try {
                executarGerenciamento();
                Thread.sleep(3600000); // Dorme por uma hora
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt(); // Marca a thread como interrompida novamente
            }
        }
    }

    public void executarGerenciamento() {
        atualizarListaDeUsos();
        calcularHorariosDeUso();
        enviarNotificacoesDeCompra();        
        excluirUsosFinalizados();        
    }

    public void atualizarListaDeUsos() {
        listaDeUsos = (ArrayList<Uso>) PessoaFisica.resgatarListaUsoMedicamentosArquivo(pessoa.getNomeArquivoUsos());
    }

    public void calcularHorariosDeUso() {
        for (Uso uso : listaDeUsos) {
            uso.calcularHorariosDeUso();
        }
    }

    public static void enviarNotificacoesDeCompra() {
        for (Uso uso : listaDeUsos) {
            if (verificarQtdRemedio(uso)) {
                enviarNotificacaoCompra(uso);
            }
        }
    }    

    public void excluirUsosFinalizados() {
        listaDeUsos.removeIf(uso -> uso.getDuracaoDoTratamento() == 0);
    }   

}
