# Web Service Clojure - Postgres

Web Service RESTful feito em clojure utilizando postgres.

O WS tem como objetivo inserir, deletar, visualizar e atualizar os dados de um grafo. Os dados são mantidos através de suas ligacões, as informações persistem em tabelas do postgres.

#Implementação
##Postgres

O principal motivo pela escolha do postgres foi para facilitar o deploy no heroku.
[Tutorial deploy heroku - Clojure/Postgres](https://devcenter.heroku.com/articles/clojure-web-application)

Pensei e cheguei a implementar duas soluções, uma delas utilizando Cassandra e a outro Mongo. Entretanto, não havia uma opção clara para utilizar um nosql, a não ser pela possível escalabilidade. O motivo pela escolha do postgres, dentre outros SGBDs relacionais, foi por acreditar que ele possui um bom desempenho.

Utilizei Compojure e Ring na solução. Minha escolha por compojure/ring foi, principalmente, por possuir muito material na internet. Olhei alguns frameworks como Liberator e Luminus, mas optei pelo compojure. Além disso estou utilizando hiccup para templates.


##Executar offline
####Requisitos
Lein

####Docker
Utilizei a imagem oficial do postgres do Docker. O acesso ao postgres foi definido na variável spec de [migration](/src/wspsql/models/migration.clj). Caso esteja usando o postgres em localhost ou o container possua outro IP, é essa variável que você deverá mudar.

####Levantar o server offline
Acessar a pasta wspsql/ e rodar o comando "lein ring server".

##Implementação

###Explicação

O desafio era construir um grafo baseado na inclusão de suas ligações. Com o grafo construído era necessário calcular a centralidade dos nós. O score de centralidade de um nó foi definido como o closeness, que é o inverso do farness, que é a soma das distâncias para todos os nós. Essa implementação eu fiz (sem estar em um web service) no [firstpart](https://github.com/BAlmeidaS/FirstPart). Entretanto, o código implementado na primeira parte, e utilizado na segunda e na terceira, se encontra nesse repositório, mais limpo e inteligivel, com mais algumas implementações, no arquivo [core](/src/wspsql/controllers/core.clj).

A terceira parte, tinha como objetivo caracterizar um nó como fraudulento. Esse nó tem seu score reduzido a zero e os scores dos outros nós do grafo são multiplicados por um fator (1 - (1/2)^k) - onde k é a distancia daquele nó para o nó fraudulento - sofrendo, portanto, um redução nos seus valores.

###Endpoints

Existem 4 endpoints:
+ **__/__** - Home do Web Service
+ **__/edges__** - Exibe e controla as edges do grafo
+ **__/graph__** - Exibe os scores dos nós do grafo
+ **__/fraud__** - Exibe e controla as fraudes dos nós do grafo

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


| *CENTRALITY*         | NO          | Closeness  | Farness |
| ------------- |:-------------:| :-----:| :-----: |
| `Data Type`     | `integer` | `numeric(8,8)` | `integer` |

Foi uma opção guardar o farness. Ele poderia sempre ser calculado.


| *FRAUD*         | no         | applied|
| ------------- |:-------------:| :-----:| 
| `Data Type`     | `integer` | `boolean` |

A tabela guarda quais nós são considerados fraudulentos. A informação de applied é para saber se a fraude referente aquele nó já foi aplicada (tanto no nó, como nos outros nós do grafo que sofrem por consequência). Ela é util para que não haja aplicação errada de fraude (aplicações múltiplas da mesma fraude, por exemplo).

###Melhoras a se fazer

1. O front-end poderia ser melhorado. Não sou particularmente bom em front-end, por isso entreguei uma versão um pouco "crua". O Visual poderia ser melhor das views, entretanto, a informação está lá e é facilmente compreendida.

2. Os POSTs realizados pelos forms não mostram resultados. Eu gostaria de ter implentando um simples alert quando os POTSs fossem realizados, informando se o dado não foi inserido por algum problema. Entretanto, tive grandes dificuldades de incluir javascript nas views, que impossibilitaram esse desenvolvimento.


#####Observações

A url /txt foi inserida, o objetivo dela é zerar o banco e cadastrar as quase mil edges do arquivo edges.txt. Antes de realizar essa insersção, cuidei para que os dados anteriores fossem deletados, bem como, os dados de centralidade e fraudes inseridas anteriormente. O objetivo da url é apenas colocar uma entrada massiva de dados de uma vez. Basta realizar um GET em /txt. A operação é um pouco demorada.









