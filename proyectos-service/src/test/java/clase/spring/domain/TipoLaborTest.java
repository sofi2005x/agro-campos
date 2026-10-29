package clase.spring.domain;

import clase.spring.domain.models.TipoLabor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TipoLaborTest {

    @Test
    void detectaFumigacionIgnorandoMayusculasYTildes() {
        assertEquals(TipoLabor.FUMIGAR, TipoLabor.desde("Fumigar"));
        assertEquals(TipoLabor.FUMIGAR, TipoLabor.desde("FUMIGACIÓN"));
        assertEquals(TipoLabor.FUMIGAR, TipoLabor.desde("Segunda fumigacion"));
    }

    @Test
    void detectaCosecha() {
        assertEquals(TipoLabor.COSECHAR, TipoLabor.desde("Cosechar"));
        assertEquals(TipoLabor.COSECHAR, TipoLabor.desde("cosecha de soja"));
    }

    @Test
    void detectaLosValoresDelEnum() {
        assertEquals(TipoLabor.ARAR, TipoLabor.desde("Arar"));
        assertEquals(TipoLabor.RASTRILLAR, TipoLabor.desde("Rastrillar"));
        assertEquals(TipoLabor.SEMBRAR, TipoLabor.desde("Sembrar"));
        assertEquals(TipoLabor.ESCARDILLAR, TipoLabor.desde("Escardillar"));
        assertEquals(TipoLabor.ROLAR, TipoLabor.desde("Rolar"));
    }

    @Test
    void nombreNuloONoReconocidoNoTieneTipo() {
        assertNull(TipoLabor.desde(null));
        assertNull(TipoLabor.desde("Otra labor"));
    }
}
