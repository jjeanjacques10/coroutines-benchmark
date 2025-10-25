# Projeto para validar o uso de coroutines

Este projeto tem como objetivo explorar e validar o uso de coroutines em Kotlin realizando benchmarks com diferentes
abordagens de concorrência.

## Estrutura do Projeto

| Caminho              | Descrição                                              |
|----------------------|--------------------------------------------------------|
| `democoroutines/`    | Código-fonte principal do projeto versão com webclient |
| `k6-test/`           | Scripts de teste de carga com K6                       |
| `local_env/`         | Configurações para ambiente local com Docker           |
| `execute_coroutines` | Script para executar os testes automatizados           |

## ⚙️ Tecnologias Utilizadas

* **Java 21** / **Kotlin**
* **Spring Boot 3.2.5**
    * WebFlux (reativo)
    * Spring Data JPA
* **Redis**
* **Docker**

## Cenários

As possibilidades de uso de coroutines estão mapeadas na classe `PaymentService.kt`, onde cada método representa um
cenário diferente de concorrência:

| Método                          | Descrição                                                                                                                                                                                                                                                                                                                                 |
|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `processAsyncCoroutineStrategy` | Processa o check‑in e a chamada ao processador de pagamento concorrentemente usando `async`/`await` dentro de `coroutineScope` (não‑bloqueante). Ideal para sobrepor operações suspensas (I/O/ chamadas remotas).                                                                                                                         |
| `processSequentialStrategy`     | Executa o check‑in e, somente após sua conclusão, invoca o processador de pagamento — execução sequencial dentro de uma função suspensa (sem concorrência entre as operações). Útil como baseline para medições seriais.                                                                                                                  |
| `blockingThreadStrategy`        | Executa as operações concorrentemente mas bloqueando a thread chamadora usando `runBlocking(Dispatchers.IO)`; usa `await()` para obter resultados e envolve a execução em `withTimeout` (ex.: 10s) para evitar bloqueios indefinidos. Mantém comportamento bloqueante para comparação, mas é uma abordagem menos recomendada em produção. |

Para usar cada estratégia, basta alterar a chamada passando o `header` correspondente na requisição HTTP:

| Estratégia                | Header HTTP               |
|---------------------------|---------------------------|
| `processAsyncCoroutineStrategy` | `strategy: ASYNC_COROUTINE` |
| `processSequentialStrategy`     | `strategy: SEQUENTIAL`          |
| `blockingThreadStrategy`        | `strategy: BLOCKING_THREAD`     |

Exemplo de requisição usando a estratégia `ASYNC_COROUTINE`:

``` shell
curl --request POST \
  --url http://localhost:9999/payments \
  --header 'Content-Type: application/json' \
  --header 'strategy: ASYNC_COROUTINE' \
  --data '{
    "correlationId": "8b2d05e3-337f-4691-aac1-37761310208e",
    "amount": "19.90",
    "requestedAt": "2025-07-27T13:06:12.892Z"
  }'
```

## Execução dos Testes

Para executar os testes de carga utilizando o K6, utilize o seguinte comando:

```bash
./execute_coroutines.sh
```

## Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests para melhorias e sugestões.

---

## Licença

Este projeto está licenciado sob a Licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## Autor

Desenvolvido por [Jean Jacques Barros](https://github.com/jjeanjacques10)
