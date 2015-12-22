# Web Service Clojure - Postgres

Web Service RESTful feito em clojure utilizando postgres.

O WS tem como objetivo inserir, deletar, visualizar e atualizar os dados de um grafo. Os dados são mantidos atraves de suas ligacoes, as informaçoes persistem na tabela edges do postgres.

#Implementação
##Postgres

O principal motivo pela escolha do postgres foi para facilitar o deploy no heroku.
[Tutorial deploy heroku - Clojure/Postgres](https://devcenter.heroku.com/articles/clojure-web-application)

Pensei e cheguei a implementar uma solução utilizando Cassandra e Mongo. Entretanto não havia uma opção clara para utilizar um nosql, a não ser pela escalabilidade. Outro motivo pela escolha do postgres foi por acreditar que ele possui um bom desempenho entre os SGBDs relacionais.

Utilizei Compojure e Ring na solução. Me escolha por compojure/ring foi por possuir muito material na internet sobre, por mais que sejam apenas libraries, achei o compojure bastante poderoso. Olhei alguns frameworks como Liberator e Luminus, mas optei pelo compojure. Além disso estou utilizando hiccup para template.


##Executar offline
####Requisitos
Lein

####Docker
Utilizei a imagem oficial do postgres do Docker. Por isso o acesso ao postgres é feito como definido na variável spec de [migration](/src/wspsql/models/migration.clj). Caso esteja usando o postgres em localhost ou o container possui outro IP, é essa variável que você deverá mudar.

####Levantar o server offline
Acessar a pasta wspsql/ e roddar o comando "lein ring server"

##Implementação

###Explicação

O desafio era construir um grafo baseado na inclusão de suas ligações. Com o grafo construído era necessário calcular a centralidade dos nós. O score de centralidade de um nó foi definido como o closeness, que é o inverso do farness, que é a soma das distâncias para todos os nós. Essa implementação eu fiz (sem estar em um web service) no [firstpart](https://github.com/BAlmeidaS/FirstPart). Entretanto, o código implementado na primeira parte e utilizado na segunda e na terceira se encontra nesse repositório, mais limpo e inteligivel, com mais algumas implementações, no arquivo [core](/src/wspsql/controllers/core.clj).

Para a terceira parte, quando um nó é caracterizado como fraudulento, tem seu score reduzido a zero, e todos os outros nós do grafo são multiplicados pela função (1 - (1/2)^k) - onde k é a distancia daquele nó para o nó fraudulento - sofrendo, portanto, um redução de seus scores.

###Endpoints

Existem 4 endpoints:
+ **__/__** - Home do Web Service
+ **__/edges__** - Exibe e controle as edges do grafo
+ **__/graph__** - Exibe os scores dos nos do grafo
+ **__/fraud__** - Exibe e controle as fraudes dos nos do grafo

###RESTful
Métodos implementados nos endpoints:
+ **__/__** - GET, HEAD, OPTIONS
+ **__/edges__** - GET, HEAD, POST, PUT, DELETE, OPTIONS
+ **__/graph__** - GET, HEAD, OPTIONS
+ **__/fraud__** - GET, HEAD, POST, PUT, DELETE, OPTIONS

###SQL e detalhes
As tabelas criadas foram:


| *EDGES*         | NO A           | NO B  | Created |
| ------------- |:-------------:| :-----:| :-----: |
| `Data Type`     | `integer` | `integer` | `timestamp` |

A coluna created guarda a data de inclusão daquele nó. Essa informação é importante pois o sistema verifica quando foi a utima inclusão de uma edge, se ela for anterior a data do o ultimo cálculo de centralidade, o sistema não recalcula a centralidade de novo, quando for requisitado. Essa otimização não tem relação com fraudes, quando uma fraude é inserida ou deletada o calculo de centralidade é feito desconsiderando essa otimização.

| *CENTRALITY*         | NO          | Closeness  | Farness |
| ------------- |:-------------:| :-----:| :-----: |
| `Data Type`     | `integer` | `numeric(8,8)` | `integer` |

Foi uma opção guardar o farness, poderia sempre ser calculado.

| *UPDATESYS*         | sys         | update|
| ------------- |:-------------:| :-----:| 
| `Data Type`     | `varchar(16)` | `timestamp` |

A coluna update guarda a data do ultimo cálculo de centralidade do sistema. Utilização explicada na tabela de EDGES.

| *FRAUD*         | no         | applied|
| ------------- |:-------------:| :-----:| 
| `Data Type`     | `integer` | `boolean` |

A tabela guarda quais nós são considerados fraudulentos. A informação de applied é para saber se a fraude referente aquele nó já foi aplicada (tanto no nó, como nos outros nós do grafo que sofrem por consequência), ela é util para que não haja aplicação errada de fraude e que multiplas fraudes possam ser aplicadas no grafo.

###Melhoras a se fazer

1. O front-end poderia ser melhorado. Não sou particularmente bom em front-end, por isso entreguei uma versão um pouco "crua". O Visual poderia ser melhor das views, entretanto, a informação está lá'e é facilmente acessada'

2. Os posts realizados pelo form não mostram resultados. Eu gostaria de ter implentando um simples alert quando os POTSs fossem realizados, informando se o dado não foi inserido por algum problema, ou se por já estar lá. Entretanto, tive grandes dificuldades de incluir javascript nas views, que impossibilitaram esse desenvolvimento.


#####Observações

A url /txt foi inserida, o objetivo dela é zerar o banco e cadastrar as mil edges do arquivo edges.txt original dentro do site. Apaga também os dados de centralidade e fraudes inseridas anteriormente, o objetivo da url é apenas colocar uma entrada massiva de dados de uma vez. Basta realizar um GET em /txt. A operação é um pouco demorada.









