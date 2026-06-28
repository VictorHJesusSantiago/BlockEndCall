package com.blockendcall.service;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OperatorLookupService {

    private static final Map<String, String> DDD_MAP = new LinkedHashMap<>();

    static {
        DDD_MAP.put("11", "São Paulo Capital"); DDD_MAP.put("12", "Vale do Paraíba");
        DDD_MAP.put("13", "Baixada Santista"); DDD_MAP.put("14", "Bauru");
        DDD_MAP.put("15", "Sorocaba"); DDD_MAP.put("16", "Ribeirão Preto");
        DDD_MAP.put("17", "São José do Rio Preto"); DDD_MAP.put("18", "Presidente Prudente");
        DDD_MAP.put("19", "Campinas"); DDD_MAP.put("21", "Rio de Janeiro");
        DDD_MAP.put("22", "Campos dos Goytacazes"); DDD_MAP.put("24", "Volta Redonda");
        DDD_MAP.put("27", "Vitória"); DDD_MAP.put("28", "Cachoeiro de Itapemirim");
        DDD_MAP.put("31", "Belo Horizonte"); DDD_MAP.put("32", "Juiz de Fora");
        DDD_MAP.put("33", "Ipatinga"); DDD_MAP.put("34", "Uberlândia");
        DDD_MAP.put("35", "Pouso Alegre"); DDD_MAP.put("37", "Divinópolis");
        DDD_MAP.put("38", "Montes Claros"); DDD_MAP.put("41", "Curitiba");
        DDD_MAP.put("42", "Ponta Grossa"); DDD_MAP.put("43", "Londrina");
        DDD_MAP.put("44", "Maringá"); DDD_MAP.put("45", "Foz do Iguaçu");
        DDD_MAP.put("46", "Francisco Beltrão"); DDD_MAP.put("47", "Joinville");
        DDD_MAP.put("48", "Florianópolis"); DDD_MAP.put("49", "Chapecó");
        DDD_MAP.put("51", "Porto Alegre"); DDD_MAP.put("53", "Pelotas");
        DDD_MAP.put("54", "Caxias do Sul"); DDD_MAP.put("55", "Santa Maria");
        DDD_MAP.put("61", "Brasília"); DDD_MAP.put("62", "Goiânia");
        DDD_MAP.put("63", "Palmas"); DDD_MAP.put("64", "Rio Verde");
        DDD_MAP.put("65", "Cuiabá"); DDD_MAP.put("66", "Rondonópolis");
        DDD_MAP.put("67", "Campo Grande"); DDD_MAP.put("68", "Rio Branco");
        DDD_MAP.put("69", "Porto Velho"); DDD_MAP.put("71", "Salvador");
        DDD_MAP.put("73", "Ilhéus"); DDD_MAP.put("74", "Juazeiro BA");
        DDD_MAP.put("75", "Feira de Santana"); DDD_MAP.put("77", "Vitória da Conquista");
        DDD_MAP.put("79", "Aracaju"); DDD_MAP.put("81", "Recife");
        DDD_MAP.put("82", "Maceió"); DDD_MAP.put("83", "João Pessoa");
        DDD_MAP.put("84", "Natal"); DDD_MAP.put("85", "Fortaleza");
        DDD_MAP.put("86", "Teresina"); DDD_MAP.put("87", "Petrolina");
        DDD_MAP.put("88", "Juazeiro do Norte"); DDD_MAP.put("89", "Picos");
        DDD_MAP.put("91", "Belém"); DDD_MAP.put("92", "Manaus");
        DDD_MAP.put("93", "Santarém"); DDD_MAP.put("94", "Marabá");
        DDD_MAP.put("95", "Boa Vista"); DDD_MAP.put("96", "Macapá");
        DDD_MAP.put("97", "Coari"); DDD_MAP.put("98", "São Luís");
        DDD_MAP.put("99", "Imperatriz");
    }

    public String lookupDdd(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 2) return "Desconhecido";
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith("55") && digits.length() > 4) digits = digits.substring(2);
        String ddd = digits.length() >= 2 ? digits.substring(0, 2) : "??";
        return DDD_MAP.getOrDefault(ddd, "Desconhecido");
    }

    public Map<String, String> getAllDdds() {
        return DDD_MAP;
    }
}
