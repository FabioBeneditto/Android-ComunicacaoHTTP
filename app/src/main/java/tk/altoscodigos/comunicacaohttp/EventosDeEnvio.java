package tk.altoscodigos.comunicacaohttp;

import org.json.JSONObject;

import java.util.List;

public interface EventosDeEnvio {

    void antesDeComunicar();

    void depoisDeEnviar(String value);

    void depoisDeEnviar(String value, JSONObject jsonObject);

    void aoFinalizar(List<JSONObject> jsonObjects, boolean houveErro);
}
