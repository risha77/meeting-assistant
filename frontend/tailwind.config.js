/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#EEEDFE',
          100: '#D5D3FD',
          500: '#534AB7',
          600: '#4338A0',
          700: '#362C82',
        },
      },
    },
  },
  plugins: [],
};
