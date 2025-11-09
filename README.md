# Desafio Técnico — Testes API (Serverest.dev)

Visão geral rápida do repositório e onde encontrar as instruções detalhadas.

Resumo
Este repositório contém a solução completa para o Desafio Técnico do Carrefour: automação de testes de API contra a plataforma pública Serverest.dev. O objetivo é validar o comportamento de todos os endpoints de usuários, garantir a segurança via JWT e assegurar que as regras de negócio (campos obrigatórios, formatos, políticas de administrador) estejam corretas, com cobertura de testes de 100% dos requisitos do PDF oficial do desafio.

Links principais
- docs/overview.md — Visão geral do desafio e escopo.
- docs/api-endpoints.md — Endpoints testados e comportamentos esperados.
- docs/validation-security.md — Regras de validação, segurança (JWT) e rate limiting.
- docs/test-strategy.md — Estratégia de testes, execução sequencial e como garantir 100% de cobertura.
- docs/installation.md — Como rodar os testes localmente.
- CHANGELOG.md — Histórico do projeto.

Status
- Objetivo atual: implementar e validar todos os requisitos do PDF do desafio com cobertura de testes 100%.
- CI: configurar pipeline para rodar testes e falhar se a cobertura for < 100%.

Como usar
- Veja docs/installation.md para configurar dependências e rodar a suíte de testes.
- Veja docs/test-strategy.md para entender como os casos de teste mapeiam os requisitos do desafio e como é garantida a cobertura de 100%.

Contato
Autor / Maintainers: (adicione informações de contato aqui)
