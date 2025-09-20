module.exports = {
  extends: ['next/core-web-vitals'],
  rules: {
    // Disable strict rules for production build
    '@typescript-eslint/no-explicit-any': 'warn',
    '@typescript-eslint/no-unused-vars': 'warn',
    'react/no-unescaped-entities': 'warn',
    '@next/next/no-img-element': 'warn',
    'jsx-a11y/alt-text': 'warn',
    'react-hooks/exhaustive-deps': 'warn',
    '@typescript-eslint/no-require-imports': 'warn',
    '@typescript-eslint/no-empty-object-type': 'warn',
    'import/no-anonymous-default-export': 'warn'
  }
}