package org.tes.hkx.tools;

/**
 * Created by Bet on 10.03.2016.
 */
public class HkCRC {
    public int crc_order = 32;
    public String crc_poly = "4C11DB7";
    public String initial_value = "000000";
    public String final_value = "000000";
    public int reflectByte(int c)
    {
        int outbyte=0;
        int i=0x01;
        int j;

        for (j=0x80; j>0; j>>=1)
        {
            int val = c & i;
            if (val > 0) outbyte|=j;
            i<<=1;
        }
        return (outbyte);
    }
    public int[] reflect(int[] crc,int bitnum,int startLSB)
    {
        int i, j, k, iw, jw, bit;

        for (k=0; k+startLSB<bitnum-1-k; k++) {

            iw=7-((k+startLSB)>>3);
            jw=1<<((k+startLSB)&7);
            i=7-((bitnum-1-k)>>3);
            j=1<<((bitnum-1-k)&7);

            bit = crc[iw] & jw;
            if ((crc[i] & j)>0) crc[iw] |= jw;
            else crc[iw] &= (0xff-jw);
            if ((bit)>0) crc[i] |= j;
            else crc[i] &= (0xff-j);
        }
        return(crc);
    }
    public String compute(String input)    {
        // computes crc value
        int i,j,k,bit,datalen,len,flag,counter,c,order,ch,actchar;
        String data,output;
        int[] mask = new int[8];
        String[] hexnum = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        int[] polynom = {0,0,0,0,4,193,29,183};
        int[] init = {0,0,0,0,0,0,0,0};
        int[] crc = {0,0,0,0,0,0,0,0,0};
        int[] xor = {0,0,0,0,0,0,0,0};
        order = this.crc_order;

        // generate bit mask
        counter = order;
        for (i=7; i>0; i--)
        {
            if (counter>=8) mask[i] = 255;
            else mask[i]=(1<<counter)-1;
            counter-=8;
            if (counter<0) counter=0;
        }
        data = input;
        datalen = data.length();
        len=0;
        for (i=0; i<datalen; i++)
        {
            c = (int) data.charAt(i);
            if (data.charAt(i)=='%')				// unescape byte by byte (%00 allowed)
            {
                if (i>datalen-3)
                    System.out.println("Invalid data sequence");

                try {
                    ch =(int) data.charAt(++i);
                    c = (int) data.charAt(++i);
                    c = (c&15) | ((ch&15)<<4);
                }
                catch (Exception e) {
                    System.out.println("Invalid data sequence");
                    return "failure";
                }

            }
            c = reflectByte(c);
            for (j=0; j<8; j++)
            {
                bit=0;
                if ((crc[7-((order-1)>>3)] & (1<<((order-1)&7)))>0) bit=1;
                if ((c&0x80)>0) bit^=1;
                c<<=1;
                for (k=0; k<8; k++)		// rotate all (max.8) crc bytes
                {
                    crc[k] = ((crc [k] << 1) | (crc [k+1] >> 7)) & mask [k];
                    if ((bit)>0) crc[k]^= polynom[k];
                }
            }
            len++;
        }
        crc = reflect(crc, order, 0);
        for (i=0; i<8; i++) crc [i] ^= xor [i];
        output = "";
        flag=0;
        for (i=0; i<8; i++)
        {
            actchar = crc[i]>>4;
            if (flag>0 || actchar>0)
            {
                output+= hexnum[actchar];
                flag=1;
            }

            actchar = crc[i] & 15;
            if (flag>0 || actchar>0 || i==7)
            {
                output+= hexnum[actchar];
                flag=1;
            }
        }
        return output;
    }
}
