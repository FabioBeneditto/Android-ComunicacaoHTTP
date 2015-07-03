package tk.altoscodigos.comunicacaohttp;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class EnviadorDeTransacoes extends AsyncTask<JSONObject, String, List<JSONObject>> {

    private String enderecoServidor;
    private EventosDeEnvio eventos;
    private Exception erro;

    public EnviadorDeTransacoes(String enderecoServidor) {
        this.enderecoServidor = enderecoServidor;
    }

    public void setEventos(MainActivity eventos) {
        this.eventos = eventos;
    }

    @Override
    protected List<JSONObject> doInBackground(JSONObject... trns) {

        publishProgress("Iniciando envio de " + trns.length + " transações");
        ArrayList<JSONObject> respostas = new ArrayList<JSONObject>();
        int nrTransacao = 1;

        for (JSONObject obj : trns) {

            try {
                JSONObject resposta = enviaTransacao(obj.toString());
                publishProgress("Enviei transaçao # " + nrTransacao++, resposta.toString());
                respostas.add(resposta);
            } catch (Exception e) {
                erro = e;
                publishProgress("ERRO: " + e.getMessage());
            }
        }

        return respostas;
    }

    private JSONObject enviaTransacao(final String transacao) throws IOException, JSONException {

        ContentProducer cp = new ContentProducer() {

            public void writeTo(OutputStream outstream) throws IOException {
                Writer writer = new OutputStreamWriter(outstream, "UTF-8");
                writer.write(transacao);
                writer.flush();
            }
        };

        HttpEntity entity = new EntityTemplate(cp);
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(enderecoServidor);
        httppost.setEntity(entity);

        HttpResponse response = httpClient.execute(httppost);
        entity = response.getEntity();

        if (entity != null) {

            InputStream is = entity.getContent();
            StringBuilder r = new StringBuilder(120);
            int ch;

            while ((ch = is.read()) >= 0) {
                r.append((char) ch);
            }

            if (r.length() > 0) {
                return new JSONObject(r.toString());
            } else {
                throw new IOException("Erro de comunicação (2): não recebi resposta");
            }
        } else {
            throw new IOException("Erro de comunicação (1): não recebi resposta");
        }
    }

    public Exception getErro() {
        return erro;
    }

    @Override
    protected void onPreExecute() {

        if (eventos != null) {
            eventos.antesDeComunicar();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {

        if (eventos != null) {
            try {
                eventos.depoisDeEnviar(values[0], new JSONObject(values[1]));
            } catch (JSONException e) {
                eventos.depoisDeEnviar(values[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(List<JSONObject> jsonObjects) {
        if (eventos != null) {
            eventos.aoFinalizar(jsonObjects, erro != null);
        }
    }
}
