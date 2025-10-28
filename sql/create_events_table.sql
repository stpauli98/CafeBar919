-- ============================================
-- Caffe Bar 919 - Events Table Setup
-- ============================================
-- Run this SQL in Supabase Dashboard > SQL Editor
-- URL: https://supabase.com/dashboard/project/wswizqibfqrmujlcfrav/sql

-- Create events table
CREATE TABLE IF NOT EXISTS events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title TEXT NOT NULL,
  date TEXT NOT NULL,
  time TEXT NOT NULL,
  description TEXT NOT NULL,
  type TEXT NOT NULL,
  image_url TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create index for active events queries (performance optimization)
CREATE INDEX IF NOT EXISTS idx_events_is_active ON events(is_active);

-- Create index for date-based queries (performance optimization)
CREATE INDEX IF NOT EXISTS idx_events_date ON events(date);

-- Enable Row Level Security
ALTER TABLE events ENABLE ROW LEVEL SECURITY;

-- Create policy for public read access (only active events)
DROP POLICY IF EXISTS "Public can view active events" ON events;
CREATE POLICY "Public can view active events" ON events
  FOR SELECT
  USING (is_active = true);

-- Create policy for authenticated admin access (full CRUD)
DROP POLICY IF EXISTS "Admins can do everything" ON events;
CREATE POLICY "Admins can do everything" ON events
  FOR ALL
  USING (auth.role() = 'authenticated');

-- Create function to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for auto-updating updated_at
DROP TRIGGER IF EXISTS update_events_updated_at ON events;
CREATE TRIGGER update_events_updated_at
  BEFORE UPDATE ON events
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data from current hardcoded events
INSERT INTO events (title, date, time, description, type, is_active)
VALUES
  ('Saturday Night', 'Subota, 12. Oktobar', '22:00', 'DJ set sa najboljim hitovima', 'DJ Night', true),
  ('Acoustic Night', 'Petak, 18. Oktobar', '20:00', 'Uživo akustična muzika', 'Live Music', true),
  ('Whiskey Night', 'Četvrtak, 24. Oktobar', '21:00', 'Specijalna večer sa whiskey degustacijom', 'Special Event', true)
ON CONFLICT (id) DO NOTHING;

-- Verify table creation
SELECT
  COUNT(*) as total_events,
  COUNT(*) FILTER (WHERE is_active = true) as active_events
FROM events;
