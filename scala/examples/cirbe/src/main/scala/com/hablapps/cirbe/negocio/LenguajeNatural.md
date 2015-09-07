# Descripción Simplificada de CIRBE

## Intercambio Lógico de la Declaración

Todas las declaraciones de datos básicos de operaciones están referidas a una
fecha (formato AAAAMM). Esta fecha (en adelante “Proceso”) indica desde qué
momento la información declarada es operativa en la entidad. Se admiten como
fechas válidas mes de calendario en el que se comunica el movimiento al BdE y el
anterior.

Las entidades comunican diariamente al BdE por el proceso CRGOPE la declaración
de datos básicos de sus operaciones (módulos B1 y B2). Esta declaración debe
realizarse en el momento en que se tenga conocimiento de la información, y en
cualquier caso, antes del día 5 del mes siguiente a la fecha indicada en el
campo “Proceso”.

En esta declaración de datos básicos los diferentes registros se remiten desde
la entidad al BdE sin indicar si se trata de un alta o de una variación. En el
BdE se aplicará la información recibida 1) Admitiéndola y aplicándola como alta
si no existía información anterior para esos datos y 2) Admitiéndola y
aplicándola como variación si ya existía información anterior para esos datos.

El BdE notifica a las entidades por el proceso CRGOPS el resultado de la
validación de su declaración de datos básicos de operaciones. La entidad
declarante debe comprobar si existen errores para proceder a corregirlos o si
por el contrario se han aceptado las operaciones sin errores: aceptación total,
parcial o rechazo total de la información recibida. Para cada registro se envía
una respuesta de aceptación o rechazo. Se utiliza como registro de comunicación
del rechazo/aceptación del registro el mismo tipo de registro que se utiliza en
la declaración. En este se indica el resultado en el código de situación 1 (si
es aceptado) y en los códigos de situación desde 1 hasta 15 si es rechazado.

Cuando la entidad reciba un rechazo de un registro de datos básicos, debe enviar
de nuevo el registro corregido rellenando todos los datos que en él sean
declarables.

Cuando la entidad considere que ha terminado de declarar datos básicos para la
fecha de proceso, se lo comunica al BdE enviando el registro de finalización de
la declaración de datos básicos. A partir de ese momento el BdE valida en
conjunto los datos básicos.

## Registros y Validaciones

Las relaciones persona operación se comunican con los tipos de registro DB010,
que debe cumplir las siguientes validaciones individuales:

1. El “Código de persona” es distinto de espacios.
2. El “Código de operación” es distinto de espacios.

Los datos básicos de la operación se comunican con el registro tipo DB020, que
debe cumplir las siguientes validaciones:

1. El “Código de operación” es distinto de espacios.
2. El valor de “Tipo de riesgo asociado a los derivados” es distinto de ZZZ si
“Tipo de producto” es igual a V39 o V48. En caso contrario se rechaza el
registro con código de situación R2008
3. El valor de “Tipo de riesgo asociado a los derivados” es ZZZ si “Tipo de
producto” es distinto de V39, V48, V54. En caso contrario se rechaza el registro
con código de situación R2008

Adicionalmente, deben cumplirse las siguientes validaciones interregistro:

1. Todas las operaciones que tengan un titular con la “Naturaleza de la
intervención en la operación” T12 deben tener solo uno y no tener de tipos
T13, T14, T15 T16, T17, T71. En caso contrario se comunica la inconsistencia
L2060.
2. Todas las operaciones que tengan un titular con “Naturaleza de la
intervención en la operación” T13, deben tener más de uno y no deben tener
ninguno con T12, T14, T15, T16, T17, T71. En caso contrario se comunica la
inconsistencia L2061.
