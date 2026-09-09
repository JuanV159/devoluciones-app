import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Valida RUT chileno (módulo 11), acepta con o sin puntos/guión. */
export function rutChilenoValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const raw = (control.value ?? '').toString().trim();
    if (!raw) {
      return null;
    }
    if (!esRutChilenoValido(raw)) {
      return { rutInvalido: true };
    }
    return null;
  };
}

export function esRutChilenoValido(rut: string): boolean {
  const limpio = rut.replace(/\./g, '').replace(/-/g, '').toUpperCase();
  if (!/^\d{7,8}[\dK]$/.test(limpio)) {
    return false;
  }
  const cuerpo = limpio.slice(0, -1);
  const dv = limpio.slice(-1);
  return calcularDv(cuerpo) === dv;
}

function calcularDv(cuerpo: string): string {
  let suma = 0;
  let multiplo = 2;
  for (let i = cuerpo.length - 1; i >= 0; i--) {
    suma += Number(cuerpo[i]) * multiplo;
    multiplo = multiplo === 7 ? 2 : multiplo + 1;
  }
  const resto = 11 - (suma % 11);
  if (resto === 11) {
    return '0';
  }
  if (resto === 10) {
    return 'K';
  }
  return String(resto);
}
