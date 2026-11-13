"""Resume el informe XML de JaCoCo en la salida del CI (IE 3.2.1).

Imprime la cobertura total de instrucciones y líneas del código lógico medido, y
el detalle por paquete, para ver de un vistazo qué falta por cubrir.

Uso: python3 cobertura.py <ruta-del-jacocoTestReport.xml>
"""

import os
import sys
import xml.etree.ElementTree as ET

UMBRAL = 80.0


def porcentaje(nodo, tipo):
    """Devuelve (cubierto, total, porcentaje) para un contador de JaCoCo."""
    for contador in nodo.findall("counter"):
        if contador.get("type") == tipo:
            cubierto = int(contador.get("covered", 0))
            perdido = int(contador.get("missed", 0))
            total = cubierto + perdido
            return cubierto, total, (100.0 * cubierto / total if total else 100.0)
    return 0, 0, 100.0


def main():
    ruta = sys.argv[1]
    if not os.path.exists(ruta):
        print(f"::error::No se encontró el informe de JaCoCo en {ruta}")
        return 1

    raiz = ET.parse(ruta).getroot()

    print("Cobertura por paquete (instrucciones):")
    paquetes = []
    for paquete in raiz.findall("package"):
        _, total, pct = porcentaje(paquete, "INSTRUCTION")
        if total:
            paquetes.append((pct, total, paquete.get("name", "?")))
    for pct, total, nombre in sorted(paquetes):
        print(f"  {pct:6.2f}%  ({total:5d} instr.)  {nombre}")

    inst_cub, inst_tot, inst_pct = porcentaje(raiz, "INSTRUCTION")
    lin_cub, lin_tot, lin_pct = porcentaje(raiz, "LINE")

    print()
    print(f"TOTAL instrucciones: {inst_pct:.2f}% ({inst_cub}/{inst_tot})")
    print(f"TOTAL líneas:        {lin_pct:.2f}% ({lin_cub}/{lin_tot})")

    resumen = os.environ.get("GITHUB_STEP_SUMMARY")
    if resumen:
        with open(resumen, "a", encoding="utf-8") as salida:
            salida.write("## Cobertura de pruebas unitarias\n\n")
            salida.write(f"- Instrucciones: **{inst_pct:.2f}%** ({inst_cub}/{inst_tot})\n")
            salida.write(f"- Líneas: **{lin_pct:.2f}%** ({lin_cub}/{lin_tot})\n")

    if inst_pct < UMBRAL:
        print(f"::warning::La cobertura ({inst_pct:.2f}%) está bajo el objetivo de {UMBRAL}%")
    return 0


if __name__ == "__main__":
    sys.exit(main())
