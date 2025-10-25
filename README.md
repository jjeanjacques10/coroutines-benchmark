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

