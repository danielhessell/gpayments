Imagine que eu tenha um monte de transaçoes ocorrendo numa mesma account, e varias dessas transaçoes
querendo adicionar, mudar o valor do balance da conta. Para garantir que ninguem ta mudando o valor do balance simultaneamente e
nao dar um erro de calculo, ou seja, podemos ter um problemas de condiçao de corrida(race condition), alteraçoes concorrentes,
entao podemos bloquear(lock) a escrita desse valor enquanto o balance esta sendo adicionado.