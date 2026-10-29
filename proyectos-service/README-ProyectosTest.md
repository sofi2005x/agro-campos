# Resumen de pruebas de `proyectos-service`

Este documento resume qué verifica cada prueba automatizada del módulo.

## `TipoLaborTest.java`

| Test | Qué verifica |
|---|---|
| `detectaFumigacionIgnorandoMayusculasYTildes` | Comprueba que `desde` reconoce textos relacionados con fumigar, aunque tengan mayúsculas, tildes o texto adicional, y devuelve `FUMIGAR`. |
| `detectaCosecha` | Comprueba que reconoce `"Cosechar"` y `"cosecha de soja"` como `COSECHAR`. |
| `detectaLosValoresDelEnum` | Verifica que reconoce los valores `ARAR`, `RASTRILLAR`, `SEMBRAR`, `ESCARDILLAR` y `ROLAR`. |
| `nombreNuloONoReconocidoNoTieneTipo` | Comprueba que un texto nulo o no reconocido devuelve `null`. |

## `PlanDeSiembraTest.java`

| Test | Qué verifica |
|---|---|
| `crearGeneraLasLaboresEnElOrdenDelCultivo` | Verifica que el plan se crea en estado `PLANIFICADO`, copia todas las labores y conserva su orden. |
| `crearConCultivoSinLaboresFalla` | Comprueba que no se puede crear un plan sin labores definidas. |
| `noSePuedeSaltearUnaLabor` | Verifica que no se puede iniciar una labor si hay una anterior pendiente. |
| `noSePuedeIniciarUnaLaborSiLaAnteriorSoloEstaEnEjecucion` | Comprueba que una labor anterior debe estar completada, no solamente iniciada. |
| `iniciarLaPrimeraLaborPoneElPlanEnCurso` | Verifica que iniciar la primera labor cambia el plan a `EN_CURSO`, marca la labor como `EN_EJECUCION` y registra la fecha de inicio. |
| `noSePuedeCompletarUnaLaborQueNoEstaEnEjecucion` | Comprueba que una labor pendiente no puede completarse directamente. |
| `noSePuedeIniciarDosVecesLaMismaLabor` | Verifica que una labor ya iniciada no puede iniciarse nuevamente. |
| `completarLaCosechaFinalizaElPlan` | Comprueba que completar la labor `COSECHAR` cambia el plan a `FINALIZADO` y deja de considerarlo activo. |
| `completarLaboresIntermediasNoFinalizaElPlan` | Verifica que completar una labor intermedia no finaliza el plan. |
| `unPlanFinalizadoNoAdmiteMasCambios` | Comprueba que un plan finalizado no puede cancelarse ni modificarse. |
| `elTratamientoSoloSeRegistraEnFumigaciones` | Verifica que el tratamiento se guarda únicamente en una labor `FUMIGAR`, no en otras labores. |
| `sePuedeCancelarUnPlanActivoYLiberaElLote` | Comprueba que un plan activo puede cancelarse y pasa a `CANCELADO`, quedando inactivo. |
| `unPlanCanceladoNoAdmiteIniciarLabores` | Verifica que un plan cancelado no permite iniciar nuevas labores. |
| `noSePuedeCancelarDosVeces` | Comprueba que un plan ya cancelado no puede cancelarse nuevamente. |

## `PlanDomainServiceTest.java`

| Test | Qué verifica |
|---|---|
| `sePermiteSembrarEnUnSueloApto` | Comprueba que se permite sembrar cuando el tipo de suelo del lote está entre los suelos aptos del cultivo. |
| `noSePermiteSembrarEnUnSueloNoApto` | Verifica que se rechaza la siembra cuando el suelo no es apto. |
| `loteSinPlanesEstaDisponible` | Comprueba que un lote sin planes puede utilizarse. |
| `loteConPlanPlanificadoNoEstaDisponible` | Verifica que un plan `PLANIFICADO` bloquea el lote. |
| `loteConPlanEnCursoNoEstaDisponible` | Comprueba que un plan `EN_CURSO` también bloquea el lote. |
| `loteConPlanesFinalizadosOCanceladosEstaDisponible` | Verifica que los planes `FINALIZADO` o `CANCELADO` no bloquean un lote. |
| `sePuedeFumigarConVientoSuaveYSinLluvia` | Comprueba que las condiciones climáticas favorables permiten fumigar. |
| `sePuedeFumigarConVientoEnElLimite` | Verifica que se permite fumigar cuando el viento coincide exactamente con el máximo permitido. |
| `noSePuedeFumigarConVientoSuperiorAlMaximo` | Comprueba que se rechaza la fumigación cuando el viento supera el límite del tratamiento. |
| `elVientoSoloSeEvaluaEnLaHoraDeAplicacion` | Verifica que el viento se evalúa únicamente en la primera hora de aplicación. |
| `noSePuedeFumigarSiSePrevenLluviaDentroDeLasHorasLibres` | Comprueba que se rechaza la fumigación si se prevé lluvia durante el período requerido sin lluvia. |
| `unaLloviznaDespreciableNoBloquea` | Verifica que una cantidad de lluvia inferior al umbral no impide fumigar. |
| `sinLimitesInformadosSeUsanLosValoresPorDefecto` | Comprueba que se aplican los valores predeterminados cuando el tratamiento no informa límites. |
| `elTratamientoDebeCorresponderAlCultivoDelPlan` | Verifica que el tratamiento debe pertenecer al mismo cultivo del plan. |

## `PlanDeSiembraServiceTest.java`

| Test | Qué verifica |
|---|---|
| `creaUnPlanPlanificadoConLasLaboresDelCultivo` | Comprueba que el servicio crea y persiste un plan con sus labores y datos correctos. |
| `rechazaUnCultivoNoAptoParaElSueloDelLote` | Verifica que no se crea el plan si el suelo del lote no es apto para el cultivo. |
| `rechazaUnSegundoPlanActivoEnElMismoLote` | Comprueba que no se pueden crear dos planes activos para el mismo lote. |
| `propagaLaFaltaDeLoteComoReferenciaNoEncontrada` | Verifica que la ausencia del lote se informa mediante `ReferenciaNoEncontradaException`. |
| `rechazaUnCultivoSinLaboresDefinidas` | Comprueba que el servicio rechaza cultivos sin labores. |
| `alCompletarLaCosechaElPlanSeFinalizaYElLoteQuedaLibre` | Verifica que completar la cosecha finaliza el plan y permite crear otro en el mismo lote. |
| `cancelarUnPlanLiberaElLote` | Comprueba que cancelar un plan libera el lote para una nueva siembra. |
| `elEstadoDeUnPlanSoloSePuedeCambiarACancelado` | Verifica que el estado solo puede cambiarse manualmente a `CANCELADO`, no directamente a `FINALIZADO`. |
| `noSePuedeSaltearUnaLabor` | Comprueba que el servicio respeta el orden obligatorio de las labores. |
| `noSePuedeCompletarUnaLaborQueNoSeInicio` | Verifica que no se puede completar una labor que todavía está pendiente. |
| `unaLaborNoVuelveAPendiente` | Comprueba que una labor iniciada no puede volver al estado `PENDIENTE`. |
| `fumigaConClimaFavorableYRegistraElTratamiento` | Verifica que una fumigación con clima favorable se inicia y registra el tratamiento aplicado. |
| `noFumigaConVientoFuerteYLaLaborQuedaPendiente` | Comprueba que el viento excesivo bloquea la fumigación y la labor permanece pendiente. |
| `noFumigaSiSePrevenLluvias` | Verifica que la lluvia prevista impide iniciar la fumigación. |
| `fumigarExigeIndicarElTratamiento` | Comprueba que una labor de fumigación requiere informar un tratamiento y que no se consulta el clima si falta. |
| `elTratamientoDebeSerDelCultivoDelPlan` | Verifica que no se puede aplicar un tratamiento asociado a otro cultivo. |
| `siElProveedorDeClimaFallaFumigaConAdvertencia` | Comprueba que, si falla el proveedor meteorológico, la fumigación continúa pero se agrega una advertencia. |
| `siElLoteNoTieneCoordenadasFumigaConAdvertenciaSinConsultarElClima` | Verifica que sin coordenadas se permite fumigar con advertencia y sin llamar al servicio climático. |
| `unaFumigacionFueraDeSecuenciaNoConsultaElClima` | Comprueba que una fumigación fuera de orden se rechaza antes de consultar el clima. |
