from decimal import Decimal, localcontext
from math import ulp


def reference(power, element_power, element_area, length=0.112, width=0.069,
              height=0.0055, fill=0.5, ambient=313.0, resistance_a=5.0, resistance_b=0.5):
    with localcontext() as context:
        context.prec = 100
        power, element_power, element_area, length, width, height, fill, ambient, resistance_a, resistance_b = map(
            Decimal.from_float,
            (power, element_power, element_area, length, width, height, fill, ambient, resistance_a, resistance_b)
        )
        case_rise = Decimal.from_float(3.1009) * power ** Decimal.from_float(0.8601)
        zone_rise = case_rise + Decimal.from_float(5.5413) * power ** Decimal.from_float(0.8578)
        area = 2 * (length * width + height * fill * (length + width))
        element = ambient + zone_rise * (Decimal("0.75") + Decimal("0.25") * element_power * area / (element_area * power))
        return [float(value) for value in (
            ambient + case_rise, ambient + zone_rise, ambient + (case_rise + zone_rise) / 2,
            element, element + (resistance_a + resistance_b) * element_power
        )]


if __name__ == "__main__":
    scenarios = {
        "excel": (4.3, 1.25, 0.00423),
        "small_rise": (1e-18, 2.5e-19, 1e-18),
        "small_denominator": (1e-200, 2.5e-201, 1e-200),
        "subnormal_power": (ulp(0.0), ulp(0.0), ulp(0.0)),
        "subnormal_products": (1e-300, ulp(0.0), ulp(0.0)),
        "small_numerator": (1e-300, ulp(0.0), 1e-18),
        "small_total_power": (1e-300, 1.25, 0.00423),
        "realistic_1": (1.0, 0.1, 0.002, 0.1, 0.08, 0.01, 0.5, 293.15),
        "realistic_2": (20.0, 2.0, 0.01, 0.2, 0.15, 0.02, 0.5, 298.15),
        "realistic_3": (80.0, 5.0, 0.03, 0.3, 0.2, 0.05, 0.5, 323.15),
        "small_geometry": (1e-100, 1e100, ulp(0.0), 1e-200, 1e-200, 1e-200),
        "large_geometry": (4.3, 1e-300, 1e308, 1e308, 1e308),
        "large_resistances": (4.3, 1e-308, 0.00423, 0.112, 0.069, 0.0055, 0.5, 313.0, 1e308, 1e308)
    }
    for name, arguments in scenarios.items():
        options = {"ambient": 0.0} if name in ("small_denominator", "small_numerator") else {}
        print(name, reference(*arguments, **options))
