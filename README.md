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

### Testes de Carga de Estratégias de Pagamento

Para executar testes de carga isolados para cada estratégia de processamento de pagamentos (ASYNC_COROUTINE, SEQUENTIAL, BLOCKING_THREAD):

```bash
cd k6-test
./run_payment_tests.sh
```

Este comando executará três testes separados e gerará relatórios individuais:
- `relatorio_async_coroutine.json` - Estratégia não-bloqueante com async/await
- `relatorio_sequential.json` - Estratégia sequencial (baseline)
- `relatorio_blocking_thread.json` - Estratégia com threads bloqueantes

Para executar um teste individual de uma estratégia específica:

```bash
# Testar apenas a estratégia ASYNC_COROUTINE
k6 run -e STRATEGY=ASYNC_COROUTINE k6-test/payments_test.js --summary-export=relatorio_async.json

# Testar apenas a estratégia SEQUENTIAL
k6 run -e STRATEGY=SEQUENTIAL k6-test/payments_test.js --summary-export=relatorio_sequential.json

# Testar apenas a estratégia BLOCKING_THREAD
k6 run -e STRATEGY=BLOCKING_THREAD k6-test/payments_test.js --summary-export=relatorio_blocking.json
```

Para mais detalhes, consulte [k6-test/PAYMENT_TESTS_README.md](k6-test/PAYMENT_TESTS_README.md).

### Testes da Rinha de Backend

Para executar os testes da Rinha de Backend original:

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
