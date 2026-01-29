CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    slug VARCHAR(255) UNIQUE NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    image_url TEXT,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_category_id ON products(category_id);
CREATE INDEX idx_sku ON products(sku);
CREATE INDEX idx_is_active ON products(is_active);

INSERT INTO categories (id, name, description, slug, image_url) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Electronics', 'Latest electronic devices and gadgets', 'electronics', 'https://example.com/images/categories/electronics.jpg'),
('550e8400-e29b-41d4-a716-446655440002', 'Books', 'Books across all genres and topics', 'books', 'https://example.com/images/categories/books.jpg'),
('550e8400-e29b-41d4-a716-446655440003', 'Anime & Manga', 'Anime merchandise, manga, and collectibles', 'anime-manga', 'https://example.com/images/categories/anime-manga.jpg'),
('550e8400-e29b-41d4-a716-446655440004', 'Toys', 'Toys and games for all ages', 'toys', 'https://example.com/images/categories/toys.jpg'),
('550e8400-e29b-41d4-a716-446655440005', 'Beauty', 'Beauty and personal care products', 'beauty', 'https://example.com/images/categories/beauty.jpg'),
('550e8400-e29b-41d4-a716-446655440006', 'Automotive', 'Car parts, accessories, and automotive supplies', 'automotive', 'https://example.com/images/categories/automotive.jpg'),
('550e8400-e29b-41d4-a716-446655440007', 'Food & Beverages', 'Food items and beverages', 'food-beverages', 'https://example.com/images/categories/food-beverages.jpg'),
('550e8400-e29b-41d4-a716-446655440008', 'Health', 'Health and wellness products', 'health', 'https://example.com/images/categories/health.jpg'),
('550e8400-e29b-41d4-a716-446655440009', 'Clothing', 'Apparel and fashion items', 'clothing', 'https://example.com/images/categories/clothing.jpg'),
('550e8400-e29b-41d4-a716-446655440010', 'Board Games', 'Board games and tabletop gaming', 'board-games', 'https://example.com/images/categories/board-games.jpg');

-- Insert 100 products (10 per category)
INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Wireless Bluetooth Headphones', 'Premium noise-cancelling wireless headphones with 30-hour battery life', 199.99, 'ELEC-001', 150, 'https://example.com/images/products/headphones.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('Smart Watch Pro', 'Fitness tracking smartwatch with heart rate monitor and GPS', 299.99, 'ELEC-002', 200, 'https://example.com/images/products/smartwatch.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('USB-C Fast Charger', '60W fast charging adapter for laptops and phones', 29.99, 'ELEC-003', 500, 'https://example.com/images/products/charger.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 24.99, 'ELEC-004', 300, 'https://example.com/images/products/mouse.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('Mechanical Keyboard', 'RGB backlit mechanical keyboard with blue switches', 89.99, 'ELEC-005', 180, 'https://example.com/images/products/keyboard.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('Portable Power Bank', '20000mAh power bank with fast charging support', 39.99, 'ELEC-006', 400, 'https://example.com/images/products/powerbank.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('Webcam HD 1080p', 'Full HD webcam with auto-focus and noise cancellation', 79.99, 'ELEC-007', 250, 'https://example.com/images/products/webcam.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('Bluetooth Speaker', 'Waterproof portable speaker with 360-degree sound', 59.99, 'ELEC-008', 350, 'https://example.com/images/products/speaker.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('Tablet Stand', 'Adjustable aluminum tablet stand for desk use', 19.99, 'ELEC-009', 600, 'https://example.com/images/products/stand.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('USB Hub 4-Port', 'USB 3.0 hub with 4 ports and LED indicators', 15.99, 'ELEC-010', 450, 'https://example.com/images/products/usbhub.jpg', '550e8400-e29b-41d4-a716-446655440001');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('The Great Gatsby', 'Classic American novel by F. Scott Fitzgerald', 12.99, 'BOOK-001', 200, 'https://example.com/images/products/gatsby.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('1984 by George Orwell', 'Dystopian fiction novel about totalitarian society', 14.99, 'BOOK-002', 180, 'https://example.com/images/products/1984.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('To Kill a Mockingbird', 'Harper Lee''s Pulitzer Prize-winning novel', 13.99, 'BOOK-003', 220, 'https://example.com/images/products/mockingbird.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('Pride and Prejudice', 'Jane Austen''s classic romantic novel', 11.99, 'BOOK-004', 250, 'https://example.com/images/products/pride.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('The Catcher in the Rye', 'J.D. Salinger''s coming-of-age novel', 12.99, 'BOOK-005', 190, 'https://example.com/images/products/catcher.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('Lord of the Rings Trilogy', 'Complete trilogy box set by J.R.R. Tolkien', 29.99, 'BOOK-006', 150, 'https://example.com/images/products/lotr.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('Harry Potter Complete Set', 'All 7 books in the Harry Potter series', 89.99, 'BOOK-007', 120, 'https://example.com/images/products/hp.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('The Hobbit', 'J.R.R. Tolkien''s fantasy adventure novel', 15.99, 'BOOK-008', 210, 'https://example.com/images/products/hobbit.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('Dune', 'Frank Herbert''s epic science fiction novel', 16.99, 'BOOK-009', 175, 'https://example.com/images/products/dune.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('The Alchemist', 'Paulo Coelho''s inspirational novel', 13.99, 'BOOK-010', 280, 'https://example.com/images/products/alchemist.jpg', '550e8400-e29b-41d4-a716-446655440002');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Naruto Shippuden Figure', 'Collectible Naruto action figure with accessories', 49.99, 'ANIME-001', 100, 'https://example.com/images/products/naruto-fig.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('One Piece Manga Vol. 1', 'First volume of the One Piece manga series', 9.99, 'ANIME-002', 300, 'https://example.com/images/products/onepiece-vol1.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('Attack on Titan Poster', 'Official Attack on Titan wall poster 24x36', 14.99, 'ANIME-003', 200, 'https://example.com/images/products/aot-poster.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('Dragon Ball Z T-Shirt', 'Official DBZ character design t-shirt', 24.99, 'ANIME-004', 150, 'https://example.com/images/products/dbz-tshirt.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('My Hero Academia Keychain', 'Collectible MHA character keychain set', 12.99, 'ANIME-005', 400, 'https://example.com/images/products/mha-keychain.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('Demon Slayer Art Book', 'Official Demon Slayer art collection book', 34.99, 'ANIME-006', 120, 'https://example.com/images/products/demonslayer-art.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('Studio Ghibli Totoro Plush', 'Soft Totoro plush toy from Studio Ghibli', 29.99, 'ANIME-007', 180, 'https://example.com/images/products/totoro.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('Death Note Notebook', 'Replica Death Note notebook with pen', 19.99, 'ANIME-008', 250, 'https://example.com/images/products/deathnote.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('Fullmetal Alchemist Pin Set', 'Collectible FMA enamel pin collection', 16.99, 'ANIME-009', 300, 'https://example.com/images/products/fma-pins.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('Spirited Away Wall Scroll', 'Official Spirited Away wall scroll 24x36', 22.99, 'ANIME-010', 160, 'https://example.com/images/products/spirited-scroll.jpg', '550e8400-e29b-41d4-a716-446655440003');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('LEGO Classic Building Set', '106-piece LEGO building set for creative play', 24.99, 'TOY-001', 350, 'https://example.com/images/products/lego-classic.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Nerf Blaster', 'Nerf Elite blaster with 12 darts included', 19.99, 'TOY-002', 280, 'https://example.com/images/products/nerf.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Rubik''s Cube', 'Classic 3x3 Rubik''s Cube puzzle', 9.99, 'TOY-003', 500, 'https://example.com/images/products/rubiks.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Remote Control Car', '1:18 scale RC car with 2.4GHz remote', 39.99, 'TOY-004', 200, 'https://example.com/images/products/rccar.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Jigsaw Puzzle 1000 Pieces', 'Landscape jigsaw puzzle with 1000 pieces', 14.99, 'TOY-005', 320, 'https://example.com/images/products/puzzle.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Yo-Yo Professional', 'Professional yo-yo with ball bearings', 12.99, 'TOY-006', 400, 'https://example.com/images/products/yoyo.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Building Blocks Set', 'Wooden building blocks set with 100 pieces', 29.99, 'TOY-007', 250, 'https://example.com/images/products/blocks.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Magic 8 Ball', 'Classic Magic 8 Ball fortune teller toy', 8.99, 'TOY-008', 450, 'https://example.com/images/products/magic8.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Fidget Spinner', 'Premium metal fidget spinner with LED lights', 6.99, 'TOY-009', 600, 'https://example.com/images/products/fidget.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('Slinky Metal', 'Classic metal Slinky toy', 7.99, 'TOY-010', 550, 'https://example.com/images/products/slinky.jpg', '550e8400-e29b-41d4-a716-446655440004');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Moisturizing Face Cream', 'Hydrating face cream with SPF 30 protection', 24.99, 'BEAU-001', 300, 'https://example.com/images/products/facecream.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Lipstick Set', 'Set of 6 matte lipsticks in various shades', 19.99, 'BEAU-002', 250, 'https://example.com/images/products/lipstick.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Eyeshadow Palette', '12-color eyeshadow palette with brush', 16.99, 'BEAU-003', 280, 'https://example.com/images/products/eyeshadow.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Shampoo & Conditioner Set', 'Sulfate-free shampoo and conditioner duo', 18.99, 'BEAU-004', 400, 'https://example.com/images/products/shampoo.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Face Mask Pack', 'Set of 10 hydrating sheet face masks', 14.99, 'BEAU-005', 350, 'https://example.com/images/products/mask.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Nail Polish Set', 'Set of 8 nail polishes in trendy colors', 12.99, 'BEAU-006', 320, 'https://example.com/images/products/nailpolish.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Makeup Brush Set', 'Professional 12-piece makeup brush set', 22.99, 'BEAU-007', 200, 'https://example.com/images/products/brushes.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Perfume 50ml', 'Elegant floral perfume 50ml bottle', 39.99, 'BEAU-008', 180, 'https://example.com/images/products/perfume.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Sunscreen SPF 50', 'Broad spectrum sunscreen SPF 50, 100ml', 15.99, 'BEAU-009', 450, 'https://example.com/images/products/sunscreen.jpg', '550e8400-e29b-41d4-a716-446655440005'),
('Body Lotion', 'Moisturizing body lotion with shea butter, 500ml', 11.99, 'BEAU-010', 500, 'https://example.com/images/products/lotion.jpg', '550e8400-e29b-41d4-a716-446655440005');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Car Phone Mount', 'Magnetic car phone mount with vent clip', 12.99, 'AUTO-001', 400, 'https://example.com/images/products/phone-mount.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Car Floor Mats', 'All-weather car floor mats set of 4', 34.99, 'AUTO-002', 250, 'https://example.com/images/products/mats.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Jump Starter', 'Portable car jump starter with USB charger', 79.99, 'AUTO-003', 150, 'https://example.com/images/products/jumpstarter.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Tire Pressure Gauge', 'Digital tire pressure gauge with LCD display', 15.99, 'AUTO-004', 300, 'https://example.com/images/products/tiregauge.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Car Air Freshener', 'Long-lasting car air freshener pack of 3', 8.99, 'AUTO-005', 500, 'https://example.com/images/products/freshener.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Dash Cam', '1080p HD dash cam with night vision', 89.99, 'AUTO-006', 180, 'https://example.com/images/products/dashcam.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Car Charger', 'Dual USB car charger with fast charging', 9.99, 'AUTO-007', 600, 'https://example.com/images/products/carcharger.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Steering Wheel Cover', 'Leather steering wheel cover with padding', 19.99, 'AUTO-008', 220, 'https://example.com/images/products/steering.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Car Seat Covers', 'Universal car seat covers set of 2', 29.99, 'AUTO-009', 280, 'https://example.com/images/products/seatcovers.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('Windshield Sun Shade', 'Foldable windshield sun shade for cars', 14.99, 'AUTO-010', 350, 'https://example.com/images/products/sunshade.jpg', '550e8400-e29b-41d4-a716-446655440006');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Organic Coffee Beans', 'Premium organic coffee beans, 1lb bag', 16.99, 'FOOD-001', 400, 'https://example.com/images/products/coffee.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Green Tea Bags', 'Organic green tea, 100 tea bags', 8.99, 'FOOD-002', 500, 'https://example.com/images/products/greentea.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Dark Chocolate Bar', '70% dark chocolate bar, 3.5oz', 5.99, 'FOOD-003', 600, 'https://example.com/images/products/chocolate.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Honey Jar', 'Raw organic honey, 16oz jar', 12.99, 'FOOD-004', 350, 'https://example.com/images/products/honey.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Olive Oil', 'Extra virgin olive oil, 500ml bottle', 14.99, 'FOOD-005', 300, 'https://example.com/images/products/oliveoil.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Granola Bars', 'Organic granola bars, pack of 12', 9.99, 'FOOD-006', 450, 'https://example.com/images/products/granola.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Protein Powder', 'Whey protein powder, 2lb container', 29.99, 'FOOD-007', 250, 'https://example.com/images/products/protein.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Trail Mix', 'Premium trail mix with nuts and dried fruit, 1lb', 11.99, 'FOOD-008', 380, 'https://example.com/images/products/trailmix.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Coconut Water', 'Natural coconut water, pack of 12 cans', 18.99, 'FOOD-009', 320, 'https://example.com/images/products/coconut.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Energy Drink', 'Natural energy drink, pack of 24', 24.99, 'FOOD-010', 400, 'https://example.com/images/products/energy.jpg', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Multivitamin Tablets', 'Daily multivitamin tablets, 90 count', 19.99, 'HEAL-001', 400, 'https://example.com/images/products/multivitamin.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Yoga Mat', 'Non-slip yoga mat with carrying strap', 24.99, 'HEAL-002', 300, 'https://example.com/images/products/yogamat.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Resistance Bands Set', 'Set of 5 resistance bands with handles', 16.99, 'HEAL-003', 350, 'https://example.com/images/products/resistance.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Foam Roller', 'High-density foam roller for muscle recovery', 18.99, 'HEAL-004', 250, 'https://example.com/images/products/foamroller.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Water Bottle', 'Stainless steel water bottle, 32oz', 15.99, 'HEAL-005', 500, 'https://example.com/images/products/waterbottle.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Fish Oil Supplements', 'Omega-3 fish oil capsules, 120 count', 22.99, 'HEAL-006', 280, 'https://example.com/images/products/fishoil.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Protein Shaker Bottle', 'BPA-free protein shaker with mixing ball', 9.99, 'HEAL-007', 450, 'https://example.com/images/products/shaker.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Massage Ball', 'Therapy massage ball for trigger points', 8.99, 'HEAL-008', 400, 'https://example.com/images/products/massageball.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Vitamin D3', 'Vitamin D3 supplements, 1000 IU, 120 count', 12.99, 'HEAL-009', 380, 'https://example.com/images/products/vitamind.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('First Aid Kit', 'Comprehensive first aid kit with 100+ items', 29.99, 'HEAL-010', 200, 'https://example.com/images/products/firstaid.jpg', '550e8400-e29b-41d4-a716-446655440008');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Cotton T-Shirt', '100% cotton classic fit t-shirt, multiple colors', 14.99, 'CLOT-001', 500, 'https://example.com/images/products/tshirt.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Denim Jeans', 'Classic fit denim jeans, various sizes', 49.99, 'CLOT-002', 300, 'https://example.com/images/products/jeans.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Hooded Sweatshirt', 'Fleece-lined hooded sweatshirt, unisex', 34.99, 'CLOT-003', 350, 'https://example.com/images/products/hoodie.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Running Shoes', 'Lightweight running shoes with cushioned sole', 79.99, 'CLOT-004', 200, 'https://example.com/images/products/runningshoes.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Baseball Cap', 'Adjustable baseball cap with embroidered logo', 19.99, 'CLOT-005', 400, 'https://example.com/images/products/cap.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Winter Jacket', 'Waterproof winter jacket with insulation', 89.99, 'CLOT-006', 150, 'https://example.com/images/products/jacket.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Athletic Shorts', 'Moisture-wicking athletic shorts', 24.99, 'CLOT-007', 380, 'https://example.com/images/products/shorts.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Socks Pack', 'Pack of 6 athletic socks, assorted colors', 12.99, 'CLOT-008', 600, 'https://example.com/images/products/socks.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Dress Shirt', 'Classic fit dress shirt, 100% cotton', 39.99, 'CLOT-009', 280, 'https://example.com/images/products/shirt.jpg', '550e8400-e29b-41d4-a716-446655440009'),
('Backpack', 'Durable canvas backpack with laptop compartment', 44.99, 'CLOT-010', 250, 'https://example.com/images/products/backpack.jpg', '550e8400-e29b-41d4-a716-446655440009');

INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Chess Set', 'Classic wooden chess set with board', 29.99, 'GAME-001', 200, 'https://example.com/images/products/chess.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Monopoly', 'Classic Monopoly board game', 24.99, 'GAME-002', 250, 'https://example.com/images/products/monopoly.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Scrabble', 'Word-building board game with tiles', 22.99, 'GAME-003', 220, 'https://example.com/images/products/scrabble.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Catan', 'Settlers of Catan strategy board game', 39.99, 'GAME-004', 180, 'https://example.com/images/products/catan.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Ticket to Ride', 'Train adventure board game', 44.99, 'GAME-005', 160, 'https://example.com/images/products/ticket.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Pandemic', 'Cooperative board game for 2-4 players', 34.99, 'GAME-006', 190, 'https://example.com/images/products/pandemic.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Cards Against Humanity', 'Party game for horrible people', 25.99, 'GAME-007', 300, 'https://example.com/images/products/cah.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Risk', 'Classic strategy board game of world domination', 32.99, 'GAME-008', 170, 'https://example.com/images/products/risk.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Uno', 'Classic Uno card game', 8.99, 'GAME-009', 500, 'https://example.com/images/products/uno.jpg', '550e8400-e29b-41d4-a716-446655440010'),
('Jenga', 'Classic Jenga tower stacking game', 14.99, 'GAME-010', 350, 'https://example.com/images/products/jenga.jpg', '550e8400-e29b-41d4-a716-446655440010');

-- Additional products with special states (inactive/out of stock)

-- Electronics: 1 inactive item
INSERT INTO products (name, description, price, sku, stock, image_url, category_id, is_active) VALUES
('VR Headset Gen 1', 'First generation VR headset - Discontinued model', 149.99, 'ELEC-011', 75, 'https://example.com/images/products/vr-old.jpg', '550e8400-e29b-41d4-a716-446655440001', FALSE);

-- Books: 2 inactive books
INSERT INTO products (name, description, price, sku, stock, image_url, category_id, is_active) VALUES
('Encyclopedia Set 2010', 'Complete encyclopedia set from 2010 - Out of print', 99.99, 'BOOK-011', 20, 'https://example.com/images/products/encyclopedia.jpg', '550e8400-e29b-41d4-a716-446655440002', FALSE),
('Dictionary 1st Edition', 'First edition dictionary - Collector''s item only', 45.99, 'BOOK-012', 15, 'https://example.com/images/products/dictionary-old.jpg', '550e8400-e29b-41d4-a716-446655440002', FALSE);

-- Anime: 3 inactive items
INSERT INTO products (name, description, price, sku, stock, image_url, category_id, is_active) VALUES
('Bleach Figure Series 1', 'Original Bleach figure - No longer in production', 39.99, 'ANIME-011', 25, 'https://example.com/images/products/bleach-old.jpg', '550e8400-e29b-41d4-a716-446655440003', FALSE),
('Yu-Gi-Oh! Card Pack 2005', 'Vintage Yu-Gi-Oh card pack - Discontinued', 19.99, 'ANIME-012', 30, 'https://example.com/images/products/yugioh-old.jpg', '550e8400-e29b-41d4-a716-446655440003', FALSE),
('Pokemon Poster Gen 1', 'Original Generation 1 Pokemon poster - Out of print', 24.99, 'ANIME-013', 18, 'https://example.com/images/products/pokemon-old.jpg', '550e8400-e29b-41d4-a716-446655440003', FALSE);

-- Toys: 4 inactive items
INSERT INTO products (name, description, price, sku, stock, image_url, category_id, is_active) VALUES
('Tamagotchi Original', 'Original Tamagotchi virtual pet - Vintage model', 29.99, 'TOY-011', 12, 'https://example.com/images/products/tamagotchi-old.jpg', '550e8400-e29b-41d4-a716-446655440004', FALSE),
('Beyblade First Series', 'First series Beyblade - No longer manufactured', 34.99, 'TOY-012', 22, 'https://example.com/images/products/beyblade-old.jpg', '550e8400-e29b-41d4-a716-446655440004', FALSE),
('Hot Wheels Track Set 2010', 'Vintage Hot Wheels track set - Discontinued', 44.99, 'TOY-013', 15, 'https://example.com/images/products/hotwheels-old.jpg', '550e8400-e29b-41d4-a716-446655440004', FALSE),
('Action Figure Vintage', 'Classic action figure from 2008 - Out of production', 19.99, 'TOY-014', 28, 'https://example.com/images/products/action-old.jpg', '550e8400-e29b-41d4-a716-446655440004', FALSE);

-- Beauty: 1 product not in stock
INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Limited Edition Perfume', 'Exclusive limited edition perfume - Currently unavailable', 79.99, 'BEAU-011', 0, 'https://example.com/images/products/perfume-limited.jpg', '550e8400-e29b-41d4-a716-446655440005');

-- Automotive: 2 products not in stock
INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Premium Car Wax', 'Professional grade car wax - Restocking soon', 24.99, 'AUTO-011', 0, 'https://example.com/images/products/carwax.jpg', '550e8400-e29b-41d4-a716-446655440006'),
('LED Headlight Kit', 'High-performance LED headlight conversion kit - Out of stock', 129.99, 'AUTO-012', 0, 'https://example.com/images/products/led-headlight.jpg', '550e8400-e29b-41d4-a716-446655440006');

-- Food & Beverages: 3 products not in stock
INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Artisan Bread Mix', 'Premium artisan bread mix - Temporarily unavailable', 13.99, 'FOOD-011', 0, 'https://example.com/images/products/bread-mix.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Imported Tea Collection', 'Rare imported tea collection - Awaiting restock', 34.99, 'FOOD-012', 0, 'https://example.com/images/products/tea-collection.jpg', '550e8400-e29b-41d4-a716-446655440007'),
('Gourmet Spice Set', 'Premium gourmet spice collection - Out of stock', 29.99, 'FOOD-013', 0, 'https://example.com/images/products/spice-set.jpg', '550e8400-e29b-41d4-a716-446655440007');

-- Health: 4 products not in stock
INSERT INTO products (name, description, price, sku, stock, image_url, category_id) VALUES
('Probiotic Supplements', 'Advanced probiotic supplements - Restocking soon', 27.99, 'HEAL-011', 0, 'https://example.com/images/products/probiotic.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Premium Yoga Block Set', 'High-density yoga block set - Currently unavailable', 19.99, 'HEAL-012', 0, 'https://example.com/images/products/yoga-blocks.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Acupressure Mat', 'Therapeutic acupressure mat - Out of stock', 44.99, 'HEAL-013', 0, 'https://example.com/images/products/acupressure.jpg', '550e8400-e29b-41d4-a716-446655440008'),
('Collagen Peptides', 'Hydrolyzed collagen peptides powder - Temporarily unavailable', 34.99, 'HEAL-014', 0, 'https://example.com/images/products/collagen.jpg', '550e8400-e29b-41d4-a716-446655440008');

-- Clothing: 1 product inactive AND not in stock
INSERT INTO products (name, description, price, sku, stock, image_url, category_id, is_active) VALUES
('Vintage Leather Jacket', 'Vintage leather jacket 2015 model - Discontinued and sold out', 199.99, 'CLOT-011', 0, 'https://example.com/images/products/leather-vintage.jpg', '550e8400-e29b-41d4-a716-446655440009', FALSE);

-- Board Games: 2 products inactive AND not in stock
INSERT INTO products (name, description, price, sku, stock, image_url, category_id, is_active) VALUES
('Axis & Allies 1st Edition', 'First edition Axis & Allies - Out of print and unavailable', 89.99, 'GAME-011', 0, 'https://example.com/images/products/axis-allies-old.jpg', '550e8400-e29b-41d4-a716-446655440010', FALSE),
('Dungeons & Dragons Basic Set', 'Original D&D basic set - Collector''s item, no stock', 149.99, 'GAME-012', 0, 'https://example.com/images/products/dnd-basic.jpg', '550e8400-e29b-41d4-a716-446655440010', FALSE);
