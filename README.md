1️⃣ Visão Geral
Este repositório contém a solução completa para o Desafio Técnico do Carrefour, que consiste em automatizar testes de API contra a plataforma pública Serverest.dev.

Objetivo: validar o comportamento de todos os endpoints de usuários, garantir a segurança via JWT e assegurar que as regras de negócio (campos obrigatórios, formatos, políticas de admin) estejam corretas.
Escopo: 5 endpoints RESTful (GET, POST, PUT, DELETE e login) + validações de entrada.
Resultado: 12 testes automatizados que cobrem 100 % dos requisitos do PDF oficial do desafio.
2️⃣ API Serverest.dev – Endpoints Testados
Método	Endpoint	Descrição	Status esperado
GET	/users	Lista todos os usuários	200 OK
POST	/users	Cria um novo usuário (todos os campos STRING)	201 Created ou 400 Bad Request (política admin)
GET	/users/{id}	Detalhes de um usuário específico	200 OK ou 400 Bad Request
PUT	/users/{id}	Atualiza dados de um usuário	200 OK ou 400 Bad Request
DELETE	/users/{id}	Remove um usuário	200 OK ou 400 Bad Request
POST	/login	Autenticação – devolve JWT	200 OK (token) ou 401 Unauthorized

Exportar

Copiar
Observação: Todos os campos (nome, email, password, administrador) são STRING. O campo administrador aceita "true" ou "false" (texto, não boolean).

3️⃣ Requisitos de Validação (conforme PDF)
Campo	Tipo	Obrigatório	Regra de validação
nome	String	✅ Sim	Não vazio
email	String	✅ Sim	Formato válido (usuario@dominio.com) e único
password	String	✅ Sim	Mínimo 6 caracteres
administrador	String	✅ Sim	"true" ou "false"

Exportar

Copiar
Segurança
JWT é obrigatório para POST /users, PUT /users/{id} e DELETE /users/{id}.
Sem token → 401 Unauthorized.
Credenciais inválidas no login → 401 Unauthorized.
Rate Limiting
Máximo 100 requisições por minuto.
Os testes são executados sequencialmente, garantindo que o limite nunca seja ultrapassado.
