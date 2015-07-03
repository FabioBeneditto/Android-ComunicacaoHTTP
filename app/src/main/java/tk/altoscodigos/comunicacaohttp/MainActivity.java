package tk.altoscodigos.comunicacaohttp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements EventosDeEnvio {

    private EnviadorDeTransacoes enviadorDeTransacoes;

    private TextView status;
    private ProgressBar pgBar;
    private Button btStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btStart = (Button) findViewById(R.id.btEnviar);
        status = (TextView) findViewById(R.id.txStatus);
        pgBar = (ProgressBar) findViewById(R.id.pgBar);
    }

    public void enviaTransacao(View v) {

        /*
          O endereço onde está rodando o WebService.  Para que vc possa testar, caso o servidor
          esteja na mesma máquina, não é possível utilizar localhost (nem 127.0.0.1).  Você
          precisará usar o endereço IP local. Para obtê-lo, utilize o comando ipconfig no windows
          ou ifconfig no linux, à partir de uma interface de comando.
        */
        String endereco = "http://10.40.60.26:8080/WSFeevale/envio.andrtrn";

        JSONObject[] transacoes = criaTransacoesDeTeste();

        pgBar.setMax(transacoes.length);
        pgBar.setProgress(0);

        enviadorDeTransacoes = new EnviadorDeTransacoes(endereco);
        enviadorDeTransacoes.setEventos(this);
        enviadorDeTransacoes.execute(transacoes);

    }

    private JSONObject[] criaTransacoesDeTeste() {

        /*
        Aqui vou criar 3 transações de exemplo.
         */
        JSONObject[] result = new JSONObject[3];

        try {
            result[0] = criaTransacaoDeTeste(1, "Primeiro Teste");
            result[1] = criaTransacaoDeTeste(3, "Teste segundo");
            result[2] = criaTransacaoDeTeste(5, "Última transação");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private JSONObject criaTransacaoDeTeste(int nro, String msg) throws JSONException {

        JSONObject novo = new JSONObject();
        novo.put("codigo", nro);
        novo.put("mensagem", msg);

        return novo;
    }

    @Override
    public void antesDeComunicar() {

        status.setText("Iniciando a brincadeira...");
    }

    @Override
    public void depoisDeEnviar(String value) {
        status.setText(value);
    }

    @Override
    public void depoisDeEnviar(String value, JSONObject jsonObject) {

        status.setText(value);
        // aqui você teria o retorno da transação e poderia tratar o resultado
        // por enquanto vou apenas dar um System.out...

        System.out.println(jsonObject.toString());

        pgBar.setProgress(pgBar.getProgress() + 1);
    }

    @Override
    public void aoFinalizar(List<JSONObject> jsonObjects, boolean houveErro) {

        // aqui é o método chamado no final da comunicação.  Você tem um List com todas as
        // transações de resposta. Aqui simplesmente estou mostrando um Toast com o status.

        if (houveErro) {
            String msg = "Comunicação terminou com erro: " + enviadorDeTransacoes.getErro().getMessage();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } else {
            String msg = "Fim de comunicação";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }
}
