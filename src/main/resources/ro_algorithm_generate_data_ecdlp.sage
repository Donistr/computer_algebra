#from sagemath import *





if __name__ == "__main__":
    with open("ro_alg_data.txt", "w") as f:
        for bits in range(10, 31):
            for _ in range(100):
                while True:
                    try:
                        p = random_prime(2 ^ bits, lbound=2 ^ (bits - 1))

                        F = GF(p)

                        while True:
                            a = F(randint(0, p - 1))
                            b = F(randint(0, p - 1))
                            if (4 * a ^ 3 + 27 * b ^ 2) != 0:
                                break

                        E = EllipticCurve(F, [a, b])

                        while True:
                            P = E.random_point()
                            if P != E(0):  # Check if P is not the point at infinity
                                break
                        Px, Py = P.xy()

                        while True:
                            k = randint(1, p - 1)
                            Q = k * P
                            if Q != E(0):
                                break
                        Qx, Qy = Q.xy()

                        f.write(f"{int(p)} {int(a)} {int(b)} {int(Px)} {int(Py)} {int(Qx)} {int(Qy)} {int(Q.log(P))}\n")
                        break
                    except:
                        m=1
