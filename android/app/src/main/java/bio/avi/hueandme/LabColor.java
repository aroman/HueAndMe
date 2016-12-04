package bio.avi.hueandme;

/**
 * Created by avi on 11/24/16.
 * Adapted from Stanford's Categorical Color Components (C3)
 * https://github.com/StanfordHCI/c3
 */


public class LabColor {

    public double L;
    public double a;
    public double b;

    public LabColor(double L, double a, double b) {
        this.L = L;
        this.a = a;
        this.b = b;
    }

    public String toString() {
        return ((int)L)+","+((int)a)+","+((int)b);
    }

    public boolean equals(Object o) {
        if (o instanceof LabColor) {
            LabColor y = (LabColor) o;
            return L==y.L && a==y.a && b==y.b;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int x = (int) L;
        int y = (int) (a + 110);
        int z = (int) (b + 110);
        return (x<<16) | (y<<8) | z;
    }

    /**
     * Maps an RGB triple to binned LAB space (D65).
     * Binning is done by <i>rounding</i> LAB values.
     */
    public static LabColor fromRGBr(int ri, int gi, int bi, double binSize) {
        // first, normalize RGB values
        double r = ri / 255.0;
        double g = gi / 255.0;
        double b = bi / 255.0;

        // D65 standard referent
        double X = 0.950470, Y = 1.0, Z = 1.088830;

        // second, map sRGB to CIE XYZ
        r = r <= 0.04045 ? r/12.92 : Math.pow((r+0.055)/1.055, 2.4);
        g = g <= 0.04045 ? g/12.92 : Math.pow((g+0.055)/1.055, 2.4);
        b = b <= 0.04045 ? b/12.92 : Math.pow((b+0.055)/1.055, 2.4);
        double x = (0.4124564*r + 0.3575761*g + 0.1804375*b) / X,
                y = (0.2126729*r + 0.7151522*g + 0.0721750*b) / Y,
                z = (0.0193339*r + 0.1191920*g + 0.9503041*b) / Z;

        // third, map CIE XYZ to CIE L*a*b* and return
        x = x > 0.008856 ? Math.pow(x, 1.0/3) : 7.787037*x + 4.0/29;
        y = y > 0.008856 ? Math.pow(y, 1.0/3) : 7.787037*y + 4.0/29;
        z = z > 0.008856 ? Math.pow(z, 1.0/3) : 7.787037*z + 4.0/29;

        double L = 116*y - 16,
                A = 500*(x-y),
                B = 200*(y-z);

        if (binSize > 0) {
            L = binSize * Math.round(L / binSize);
            A = binSize * Math.round(A / binSize);
            B = binSize * Math.round(B / binSize);
        }
        return new LabColor(L,A,B);
    }

    public static double ciede2000(LabColor x, LabColor y) {
        // adapted from Sharma et al's MATLAB implementation at
        //  http://www.ece.rochester.edu/~gsharma/ciede2000/

        // parametric factors, use defaults
        double kl = 1, kc = 1, kh = 1;

        // compute terms
        double pi = Math.PI,
                L1 = x.L, a1 = x.a, b1 = x.b, Cab1 = Math.sqrt(a1*a1 + b1*b1),
                L2 = y.L, a2 = y.a, b2 = y.b, Cab2 = Math.sqrt(a2*a2 + b2*b2),
                Cab = 0.5*(Cab1 + Cab2),
                G = 0.5*(1 - Math.sqrt(Math.pow(Cab,7)/(Math.pow(Cab,7)+Math.pow(25,7)))),
                ap1 = (1+G) * a1,
                ap2 = (1+G) * a2,
                Cp1 = Math.sqrt(ap1*ap1 + b1*b1),
                Cp2 = Math.sqrt(ap2*ap2 + b2*b2),
                Cpp = Cp1 * Cp2;

        // ensure hue is between 0 and 2pi
        double hp1 = Math.atan2(b1, ap1); if (hp1 < 0) hp1 += 2*pi;
        double hp2 = Math.atan2(b2, ap2); if (hp2 < 0) hp2 += 2*pi;

        double dL = L2 - L1,
                dC = Cp2 - Cp1,
                dhp = hp2 - hp1;

        if (dhp > +pi) dhp -= 2*pi;
        if (dhp < -pi) dhp += 2*pi;
        if (Cpp == 0) dhp = 0;

        // Note that the defining equations actually need
        // signed Hue and chroma differences which is different
        // from prior color difference formulae
        double dH = 2 * Math.sqrt(Cpp) * Math.sin(dhp/2);

        // Weighting functions
        double Lp = 0.5 * (L1 + L2),
                Cp = 0.5 * (Cp1 + Cp2);

        // Average Hue Computation
        // This is equivalent to that in the paper but simpler programmatically.
        // Average hue is computed in radians and converted to degrees where needed
        double hp = 0.5 * (hp1 + hp2);
        // Identify positions for which abs hue diff exceeds 180 degrees
        if (Math.abs(hp1-hp2) > pi) hp -= pi;
        if (hp < 0) hp += 2*pi;

        // Check if one of the chroma values is zero, in which case set
        // mean hue to the sum which is equivalent to other value
        if (Cpp == 0) hp = hp1 + hp2;

        double Lpm502 = (Lp-50) * (Lp-50),
                Sl = 1 + 0.015*Lpm502 / Math.sqrt(20+Lpm502),
                Sc = 1 + 0.045*Cp,
                T = 1 - 0.17*Math.cos(hp - pi/6)
                        + 0.24*Math.cos(2*hp)
                        + 0.32*Math.cos(3*hp+pi/30)
                        - 0.20*Math.cos(4*hp - 63*pi/180),
                Sh = 1 + 0.015 * Cp * T,
                ex = (180/pi*hp-275) / 25,
                delthetarad = (30*pi/180) * Math.exp(-1 * (ex*ex)),
                Rc =  2 * Math.sqrt(Math.pow(Cp,7) / (Math.pow(Cp,7) + Math.pow(25,7))),
                RT = -1 * Math.sin(2*delthetarad) * Rc;

        dL = dL / (kl*Sl);
        dC = dC / (kc*Sc);
        dH = dH / (kh*Sh);

        // The CIE 00 color difference
        return Math.sqrt(dL*dL + dC*dC + dH*dH + RT*dC*dH);
    }

}
