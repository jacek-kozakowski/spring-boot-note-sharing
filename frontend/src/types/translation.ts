export const Language = {
  PL: 'PL',
  EN: 'EN',
  DE: 'DE',
  FR: 'FR',
  ES: 'ES',
  IT: 'IT',
  RU: 'RU'
} as const;

export type Language = typeof Language[keyof typeof Language];

export const LANGUAGE_OPTIONS = [
  { value: Language.EN, label: 'English' },
  { value: Language.PL, label: 'Polish' },
  { value: Language.DE, label: 'German' },
  { value: Language.FR, label: 'French' },
  { value: Language.ES, label: 'Spanish' },
  { value: Language.IT, label: 'Italian' },
  { value: Language.RU, label: 'Russian' }
];
