export const exampleCalculation = `
**Example Calculations:**

1. **Article Name**: "Catsan Hygiene plus 20L"
   - **Id**: "123"
   - **Extracted Unit**: "ml" (since 1L = 1000ml)
   - **Quantity_in_unit**: 20,000 (20L × 1000ml/L)
   - **Price**: 19.95
   - **Base Unit**: 100 (per 100ml)

   **Calculation:**
   \`\`\`
   price_base_unit = (Price / Quantity_in_unit) × Base Unit
   price_base_unit = (19.95 / 20000) × 100 = 0.09975
   \`\`\`

2. **Article Name**: "Balsamico Dressing 60ML"
   - **Id**: "456"
   - **Extracted Unit**: "ml"
   - **Quantity_in_unit**: 60
   - **Price**: 0.70
   - **Base Unit**: 100 (per 100ml)

   **Calculation:**
   \`\`\`
   price_base_unit = (0.70 / 60) × 100 ≈ 1.1667
   \`\`\`

3. **Article Name**: "Danone Actimel Classic 0% 10×100g"
   - **Id**: "555"
   - **Extracted Unit**: "g"
   - **Quantity_in_unit**: 1,000 (10 × 100g)
   - **Price**: 8.20
   - **Base Unit**: 100 (per 100g)

   **Calculation:**
   \`\`\`
   price_base_unit = (8.20 / 1000) × 100 = 0.8200
   \`\`\`

4. **Article Name**: "Q&P Eier Freiland Schweiz 10St 53g+"
   - **Id**: "678"
   - **Extracted Unit**: "stk"
   - **Quantity_in_unit**: 10
   - **Price**: 4.95
   - **Base Unit**: 1

   **Calculation:**
   \`\`\`
   price_base_unit = (4.95 / 10) × 1 = 0.495
   \`\`\`

5. **Article Name**: "Coop Pouletbrust 2St. ca. 330g"
   - **Id**: "789"
   - **Extracted Unit**: "g"
   - **Quantity_in_unit**: 330
   - **Price**: 12.80
   - **Base Unit**: 100 (per 100g)

   **Calculation:**
   \`\`\`
   price_base_unit = (12.80 / 330) × 100 ≈ 3.8788
   \`\`\`

6. **Article Name**: "Calanda Radler Alpen Hugo 0.0 DS 6×33CL"
   - **Id**: "88"
   - **Extracted Unit**: "ml" (since 1cl = 10ml)
   - **Quantity_in_unit**: 1,980 (6 × 33cl × 10ml per cl)
   - **Price**: 8.50
   - **Base Unit**: 100 (per 100ml)

   **Calculation:**
   \`\`\`
   price_base_unit = (8.50 / 1980) × 100 ≈ 0.4293
   \`\`\`
`;

export const exampleOutput = [
  {
    id: "123",
    unit: "ml",
    base_unit: 100,
    price_base_unit: 0.09975,
  },
  {
    id: "456",
    unit: "ml",
    base_unit: 100,
    price_base_unit: 1.1667,
  },
  {
    id: "555",
    unit: "g",
    base_unit: 100,
    price_base_unit: 0.82,
  },
  {
    id: "678",
    unit: "stk",
    base_unit: 1,
    price_base_unit: 0.495,
  },
  {
    id: "789",
    unit: "g",
    base_unit: 100,
    price_base_unit: 3.8788,
  },
  {
    id: "88",
    unit: "ml",
    base_unit: 100,
    price_base_unit: 0.4293,
  },
];
